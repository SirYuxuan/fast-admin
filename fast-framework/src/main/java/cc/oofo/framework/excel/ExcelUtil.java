package cc.oofo.framework.excel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel 导入导出工具
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Slf4j
public class ExcelUtil {

    private static final String XLSX = ".xlsx";

    /**
     * 导出到 HttpServletResponse
     */
    public static <T> void export(HttpServletResponse response, String fileName,
                                  Class<T> clazz, List<T> data) {
        List<FieldMeta> metas = collectFields(clazz, Excel.Type.IMPORT_ONLY);
        try {
            setExcelResponse(response, fileName);

            // 表头：单列单行
            List<List<String>> head = new ArrayList<>();
            for (FieldMeta m : metas) {
                head.add(List.of(m.headerName));
            }

            // 转换 List<T> → List<List<Object>>（按列顺序排好）
            List<List<Object>> rows = new ArrayList<>();
            if (data != null) {
                for (T item : data) {
                    List<Object> row = new ArrayList<>();
                    for (FieldMeta m : metas) {
                        row.add(formatCell(m, getFieldValue(item, m.field)));
                    }
                    rows.add(row);
                }
            }

            ExcelWriter writer = EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .registerWriteHandler(buildStyleHandler())
                    .registerWriteHandler(buildColumnWidthHandler(metas))
                    .build();
            WriteSheet sheet = EasyExcel.writerSheet(0, "Sheet1").build();
            writer.write(rows, sheet);
            writer.finish();
        } catch (IOException e) {
            throw new ExcelException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 下载导入模板（只有表头 + 一行示例）
     */
    public static <T> void exportTemplate(HttpServletResponse response, String fileName,
                                          Class<T> clazz) {
        List<FieldMeta> metas = collectFields(clazz, Excel.Type.EXPORT_ONLY);
        try {
            setExcelResponse(response, fileName);
            List<List<String>> head = new ArrayList<>();
            for (FieldMeta m : metas) {
                String h = m.headerName + (m.required ? " *" : "");
                head.add(List.of(h));
            }
            // 示例行（取注解 sample 字段）
            List<List<Object>> rows = new ArrayList<>();
            List<Object> sampleRow = new ArrayList<>();
            boolean hasSample = false;
            for (FieldMeta m : metas) {
                sampleRow.add(m.sample);
                if (m.sample != null && !m.sample.isEmpty()) hasSample = true;
            }
            if (hasSample) rows.add(sampleRow);

            ExcelWriter writer = EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .registerWriteHandler(buildStyleHandler())
                    .registerWriteHandler(buildColumnWidthHandler(metas))
                    .build();
            WriteSheet sheet = EasyExcel.writerSheet(0, "Sheet1").build();
            writer.write(rows, sheet);
            writer.finish();
        } catch (IOException e) {
            throw new ExcelException("导出模板失败：" + e.getMessage(), e);
        }
    }

    /**
     * 导入：返回成功行 + 错误列表
     */
    public static <T> ImportResult<T> importData(MultipartFile file, Class<T> clazz) {
        ImportResult<T> result = new ImportResult<>();
        List<FieldMeta> metas = collectFields(clazz, Excel.Type.EXPORT_ONLY);
        // 表头列名 → FieldMeta
        Map<String, FieldMeta> headerMap = new HashMap<>();
        for (FieldMeta m : metas) {
            headerMap.put(m.headerName, m);
            // 兼容带 * 的必填表头
            headerMap.put(m.headerName + " *", m);
        }

        try (InputStream in = file.getInputStream()) {
            EasyExcel.read(in, new ReadListener<Map<Integer, String>>() {
                final Map<Integer, String> headIndexMap = new HashMap<>();

                @Override
                public void invokeHead(Map<Integer, com.alibaba.excel.metadata.data.ReadCellData<?>> head,
                                       com.alibaba.excel.context.AnalysisContext ctx) {
                    head.forEach((idx, cell) -> {
                        Object v = cell.getStringValue();
                        if (v != null) headIndexMap.put(idx, String.valueOf(v).trim());
                    });
                }

                @Override
                public void invoke(Map<Integer, String> rowMap,
                                   com.alibaba.excel.context.AnalysisContext ctx) {
                    int rowIndex = ctx.readRowHolder().getRowIndex() + 1; // 行号从 1 开始
                    result.setTotalRows(result.getTotalRows() + 1);

                    try {
                        T obj = clazz.getDeclaredConstructor().newInstance();
                        boolean rowOk = true;
                        for (Map.Entry<Integer, String> e : rowMap.entrySet()) {
                            String headerName = headIndexMap.get(e.getKey());
                            if (headerName == null) continue;
                            FieldMeta m = headerMap.get(headerName);
                            if (m == null) continue;

                            String raw = e.getValue();
                            // 必填校验
                            if (m.required && (raw == null || raw.isBlank())) {
                                result.getErrors().add(new ImportResult.ImportError(
                                        rowIndex, m.headerName, "必填字段不能为空"));
                                rowOk = false;
                                continue;
                            }
                            if (raw == null || raw.isBlank()) continue;

                            try {
                                Object converted = parseCell(m, raw.trim());
                                setFieldValue(obj, m.field, converted);
                            } catch (Exception ex) {
                                result.getErrors().add(new ImportResult.ImportError(
                                        rowIndex, m.headerName,
                                        "格式错误：" + ex.getMessage()));
                                rowOk = false;
                            }
                        }
                        if (rowOk) {
                            result.getSuccess().add(obj);
                            result.getSuccessRowIndexes().add(rowIndex);
                        }
                    } catch (Exception ex) {
                        result.getErrors().add(new ImportResult.ImportError(
                                rowIndex, "", "解析异常：" + ex.getMessage()));
                    }
                }

                @Override
                public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext ctx) {
                }
            }).sheet().doRead();
        } catch (Exception e) {
            log.error("import excel failed", e);
            throw new ExcelException("Excel 文件解析失败：" + e.getMessage(), e);
        }
        return result;
    }

    // ============================================================
    // 内部辅助
    // ============================================================

    private static void setExcelResponse(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encoded + XLSX);
    }

    /** 收集字段元信息（按 sort 排序），exclude 表示排除某个用途 */
    private static List<FieldMeta> collectFields(Class<?> clazz, Excel.Type excludeType) {
        List<FieldMeta> metas = new ArrayList<>();
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                Excel ann = f.getAnnotation(Excel.class);
                if (ann == null) continue;
                if (ann.type() == excludeType) continue; // 排除单向用途
                f.setAccessible(true);
                FieldMeta m = new FieldMeta();
                m.field = f;
                m.annotation = ann;
                m.headerName = ann.name().isEmpty() ? f.getName() : ann.name();
                m.sort = ann.sort();
                m.width = ann.width();
                m.required = ann.required();
                m.dictType = ann.dictType();
                m.dateFormat = ann.dateFormat();
                m.yesNo = ann.yesNo();
                m.sample = ann.sample();
                metas.add(m);
            }
            c = c.getSuperclass();
        }
        metas.sort(Comparator.comparingInt(a -> a.sort));
        return metas;
    }

    /** 单元格输出格式化 */
    private static Object formatCell(FieldMeta m, Object value) {
        if (value == null) return "";

        // 字典转换
        if (!m.dictType.isEmpty()) {
            DictResolver r = DictResolverHolder.get();
            if (r != null) {
                String label = r.resolveLabel(m.dictType, String.valueOf(value));
                return label != null ? label : value;
            }
        }
        // yes/no
        if (m.yesNo != null && m.yesNo.length >= 2) {
            if (value instanceof Boolean b) return b ? m.yesNo[1] : m.yesNo[0];
            if (value instanceof Number n) return n.intValue() == 1 ? m.yesNo[1] : m.yesNo[0];
        }
        // 日期格式
        if (value instanceof Date d) {
            return new SimpleDateFormat(m.dateFormat).format(d);
        }
        if (value instanceof Timestamp t) {
            return new SimpleDateFormat(m.dateFormat).format(t);
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.format(DateTimeFormatter.ofPattern(m.dateFormat));
        }
        if (value instanceof LocalDate ld) {
            return ld.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        // 枚举：优先 getName() / getLabel() / getDesc()，否则 name()
        if (value instanceof Enum<?> e) {
            for (String mn : new String[]{"getName", "getLabel", "getDesc"}) {
                try {
                    java.lang.reflect.Method method = e.getClass().getMethod(mn);
                    Object r = method.invoke(e);
                    if (r != null) return r.toString();
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ex) {
                    log.warn("call enum method {} failed: {}", mn, ex.getMessage());
                }
            }
            return e.name();
        }
        return value;
    }

    /** 解析单元格字符串到目标字段类型 */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object parseCell(FieldMeta m, String raw) {
        Class<?> type = m.field.getType();

        // 字典反向（label → value）
        if (!m.dictType.isEmpty()) {
            DictResolver r = DictResolverHolder.get();
            if (r != null) {
                String v = r.resolveValue(m.dictType, raw);
                if (v != null) raw = v;
            }
        }
        // yes/no
        if (m.yesNo != null && m.yesNo.length >= 2) {
            if (m.yesNo[1].equals(raw)) raw = "1";
            else if (m.yesNo[0].equals(raw)) raw = "0";
        }

        if (type == String.class) return raw;
        if (type == Integer.class || type == int.class) return Integer.valueOf(raw);
        if (type == Long.class || type == long.class) return Long.valueOf(raw);
        if (type == Double.class || type == double.class) return Double.valueOf(raw);
        if (type == Boolean.class || type == boolean.class) {
            return "1".equals(raw) || "true".equalsIgnoreCase(raw);
        }
        if (type.isEnum()) {
            return parseEnum((Class<? extends Enum>) type, raw);
        }
        return raw;
    }

    /** 枚举反查：依次尝试 name() / getName() / value() */
    @SuppressWarnings("rawtypes")
    private static Object parseEnum(Class<? extends Enum> type, String raw) {
        Enum<?>[] consts = type.getEnumConstants();
        if (consts == null) return null;
        // 1) name() 直接匹配
        for (Enum<?> c : consts) {
            if (c.name().equalsIgnoreCase(raw)) return c;
        }
        // 2) getName() / getLabel() / getDesc() 匹配
        for (Enum<?> c : consts) {
            for (String mn : new String[]{"getName", "getLabel", "getDesc"}) {
                try {
                    Object v = c.getClass().getMethod(mn).invoke(c);
                    if (v != null && raw.equals(v.toString())) return c;
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ignored) {
                }
            }
        }
        // 3) value() 匹配（如 1, 0）
        for (Enum<?> c : consts) {
            try {
                Object v = c.getClass().getMethod("value").invoke(c);
                if (v != null && raw.equals(v.toString())) return c;
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ignored) {
            }
        }
        throw new IllegalArgumentException("无法识别的枚举值: " + raw);
    }

    private static Object getFieldValue(Object obj, Field f) {
        try {
            return f.get(obj);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static void setFieldValue(Object obj, Field f, Object v) {
        try {
            f.set(obj, v);
        } catch (IllegalAccessException e) {
            throw new ExcelException("反射赋值失败：" + f.getName(), e);
        }
    }

    /** 默认表头样式 */
    private static WriteHandler buildStyleHandler() {
        WriteCellStyle headStyle = new WriteCellStyle();
        WriteFont headFont = new WriteFont();
        headFont.setFontHeightInPoints((short) 12);
        headFont.setBold(true);
        headStyle.setWriteFont(headFont);
        headStyle.setFillForegroundColor((short) 41); // 浅蓝
        return new HorizontalCellStyleStrategy(headStyle, new WriteCellStyle());
    }

    /** 列宽 handler */
    private static WriteHandler buildColumnWidthHandler(List<FieldMeta> metas) {
        return new com.alibaba.excel.write.handler.SheetWriteHandler() {
            @Override
            public void afterSheetCreate(
                    com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder workbookHolder,
                    com.alibaba.excel.write.metadata.holder.WriteSheetHolder sheetHolder) {
                for (int i = 0; i < metas.size(); i++) {
                    int width = metas.get(i).width <= 0 ? 20 : metas.get(i).width;
                    sheetHolder.getSheet().setColumnWidth(i, width * 256);
                }
            }
        };
    }

    /** 字段元信息 */
    private static class FieldMeta {
        Field field;
        Excel annotation;
        String headerName;
        int sort;
        int width;
        boolean required;
        String dictType;
        String dateFormat;
        String[] yesNo;
        String sample;
    }
}

package cc.oofo.ai.rag.service.parser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cc.oofo.framework.exception.BizException;
import cc.oofo.system.file.entity.SysFile;

/**
 * RAG 文档文本提取器。
 */
@Service
public class AiDocumentTextExtractor {

    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            "txt", "md", "markdown", "csv", "json", "xml", "html", "log", "yml", "yaml");
    private static final Set<String> WORD_EXTENSIONS = Set.of("doc", "docx");
    private static final Set<String> POWERPOINT_EXTENSIONS = Set.of("ppt", "pptx");
    private static final Set<String> EXCEL_EXTENSIONS = Set.of("xls", "xlsx");

    public String extract(SysFile file, InputStream inputStream) {
        String ext = normalizeExt(file);
        try {
            String text = switch (ext) {
                case "txt", "md", "markdown", "csv", "json", "xml", "html", "log", "yml", "yaml" ->
                    extractPlainText(inputStream);
                case "doc" -> extractDoc(inputStream);
                case "docx" -> extractDocx(inputStream);
                case "ppt" -> extractPpt(inputStream);
                case "pptx" -> extractPptx(inputStream);
                case "xls", "xlsx" -> extractWorkbook(inputStream);
                default -> throw new BizException("暂不支持解析 ." + ext
                        + " 文件，请上传 txt/md/csv/json/xml/html/yml/doc/docx/ppt/pptx/xls/xlsx 等文件");
            };
            String normalized = normalizeText(text);
            if (!StringUtils.hasText(normalized)) {
                throw new BizException("文档没有可索引文本");
            }
            return normalized;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("读取文档失败：" + e.getMessage());
        }
    }

    public boolean supports(String ext) {
        String normalized = ext == null ? "" : ext.toLowerCase();
        return TEXT_EXTENSIONS.contains(normalized)
                || WORD_EXTENSIONS.contains(normalized)
                || POWERPOINT_EXTENSIONS.contains(normalized)
                || EXCEL_EXTENSIONS.contains(normalized);
    }

    private String extractPlainText(InputStream inputStream) throws Exception {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String extractDoc(InputStream inputStream) throws Exception {
        try (HWPFDocument document = new HWPFDocument(inputStream);
                WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractDocx(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                appendLine(text, paragraph.getText());
            }
            for (XWPFTable table : document.getTables()) {
                appendTable(text, table);
            }
            return text.toString();
        }
    }

    private String extractPpt(InputStream inputStream) throws Exception {
        try (HSLFSlideShow slideShow = new HSLFSlideShow(inputStream)) {
            StringBuilder text = new StringBuilder();
            int slideIndex = 1;
            for (HSLFSlide slide : slideShow.getSlides()) {
                appendLine(text, "第 " + slideIndex++ + " 页");
                for (HSLFShape shape : slide.getShapes()) {
                    if (shape instanceof HSLFTextShape textShape) {
                        appendLine(text, textShape.getText());
                    }
                }
            }
            return text.toString();
        }
    }

    private String extractPptx(InputStream inputStream) throws Exception {
        try (XMLSlideShow slideShow = new XMLSlideShow(inputStream)) {
            StringBuilder text = new StringBuilder();
            int slideIndex = 1;
            for (XSLFSlide slide : slideShow.getSlides()) {
                appendLine(text, "第 " + slideIndex++ + " 页");
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        appendLine(text, textShape.getText());
                    }
                }
            }
            return text.toString();
        }
    }

    private String extractWorkbook(InputStream inputStream) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            StringBuilder text = new StringBuilder();
            for (Sheet sheet : workbook) {
                appendLine(text, "Sheet: " + sheet.getSheetName());
                for (Row row : sheet) {
                    List<String> values = new ArrayList<>();
                    for (Cell cell : row) {
                        String value = formatter.formatCellValue(cell, evaluator).trim();
                        if (StringUtils.hasText(value)) {
                            values.add(value);
                        }
                    }
                    if (!values.isEmpty()) {
                        appendLine(text, "第 " + (row.getRowNum() + 1) + " 行: " + String.join(" | ", values));
                    }
                }
            }
            return text.toString();
        }
    }

    private void appendTable(StringBuilder text, XWPFTable table) {
        for (XWPFTableRow row : table.getRows()) {
            List<String> values = new ArrayList<>();
            for (XWPFTableCell cell : row.getTableCells()) {
                StringBuilder cellText = new StringBuilder();
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    appendInline(cellText, paragraph.getText());
                }
                String value = cellText.toString().trim();
                if (StringUtils.hasText(value)) {
                    values.add(value);
                }
            }
            if (!values.isEmpty()) {
                appendLine(text, String.join(" | ", values));
            }
        }
    }

    private void appendLine(StringBuilder text, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        text.append(value.trim()).append('\n');
    }

    private void appendInline(StringBuilder text, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        if (!text.isEmpty()) {
            text.append(' ');
        }
        text.append(value.trim());
    }

    private String normalizeExt(SysFile file) {
        String ext = file.getExt() == null ? "" : file.getExt().trim().toLowerCase();
        if (!StringUtils.hasText(ext)) {
            throw new BizException("文件扩展名为空，无法解析");
        }
        return ext;
    }

    private String normalizeText(String text) {
        return (text == null ? "" : text)
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}

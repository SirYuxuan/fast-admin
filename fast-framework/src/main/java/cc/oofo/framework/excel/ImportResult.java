package cc.oofo.framework.excel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Excel 导入结果
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Data
@NoArgsConstructor
public class ImportResult<T> {

    /** 解析成功的数据行 */
    private List<T> success = new ArrayList<>();

    /** 解析失败的行（行号 + 错误信息） */
    private List<ImportError> errors = new ArrayList<>();

    /** Excel 总行数（不含表头） */
    private int totalRows;

    public boolean hasError() {
        return errors != null && !errors.isEmpty();
    }

    public int getSuccessCount() {
        return success == null ? 0 : success.size();
    }

    public int getErrorCount() {
        return errors == null ? 0 : errors.size();
    }

    @Data
    @NoArgsConstructor
    public static class ImportError {
        /** Excel 中的行号（从 2 开始，1 是表头） */
        private int rowIndex;
        /** 错误的列名 */
        private String column;
        /** 错误描述 */
        private String message;

        public ImportError(int rowIndex, String column, String message) {
            this.rowIndex = rowIndex;
            this.column = column;
            this.message = message;
        }
    }
}

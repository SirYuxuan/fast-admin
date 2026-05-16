package cc.oofo.system.file.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 上传文件元数据
 *
 * @author Sir丶雨轩
 */
@Data
@Builder
@AllArgsConstructor
public class FileMeta {

    /** 原始文件名 */
    private String originalName;

    /** 目标存储相对路径（含文件名），如 2026/05/15/abc.png */
    private String storageKey;

    /** 字节数 */
    private long size;

    /** MIME 类型 */
    private String contentType;

}

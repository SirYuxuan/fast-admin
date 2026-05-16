package cc.oofo.system.file.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 上传结果
 *
 * @author Sir丶雨轩
 */
@Data
@Builder
@AllArgsConstructor
public class UploadResult {

    /** 最终的存储 key（实现可基于 FileMeta.storageKey 做调整） */
    private String storageKey;

    /** 可访问的完整 URL */
    private String url;

}

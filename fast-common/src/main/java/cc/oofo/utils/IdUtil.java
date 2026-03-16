package cc.oofo.utils;

import com.github.ksuid.Ksuid;

/**
 * ID 工具类
 */
public class IdUtil {

    /**
     * 生成全局唯一 ID
     * 
     * @return 全局唯一 ID
     */
    public static String generateId() {
        return Ksuid.newKsuid().toString();
    }
}

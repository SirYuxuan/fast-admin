package cc.oofo.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码工具类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
public class PasswordUtil {

    /**
     * 创建加密密码
     * 
     * @param plainPassword 明文密码
     * @return 加密后密码
     */
    public static String create(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * 验证密码
     * 
     * @param plainPassword  明文密码
     * @param hashedPassword 加密后密码
     * @return 是否匹配
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}

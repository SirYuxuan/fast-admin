package cc.oofo.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servlet 相关工具
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
public class ServletUtil {

    /**
     * 获取当前请求
     */
    public static HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs == null ? null : attrs.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端 IP（兼容代理）
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多级代理时取第一个
            int idx = ip.indexOf(',');
            return idx == -1 ? ip.trim() : ip.substring(0, idx).trim();
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;
        ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 获取 User-Agent
     */
    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        return request == null ? "" : request.getHeader("User-Agent");
    }

    /**
     * 从 UA 简单解析浏览器
     */
    public static String parseBrowser(String ua) {
        if (ua == null) return "Unknown";
        String s = ua.toLowerCase();
        if (s.contains("edg/")) return "Edge";
        if (s.contains("chrome/")) return "Chrome";
        if (s.contains("firefox/")) return "Firefox";
        if (s.contains("safari/")) return "Safari";
        if (s.contains("opera") || s.contains("opr/")) return "Opera";
        if (s.contains("msie") || s.contains("trident")) return "IE";
        return "Other";
    }

    /**
     * 从 UA 简单解析操作系统
     */
    public static String parseOs(String ua) {
        if (ua == null) return "Unknown";
        String s = ua.toLowerCase();
        if (s.contains("windows nt 10")) return "Windows 10/11";
        if (s.contains("windows nt 6.3")) return "Windows 8.1";
        if (s.contains("windows nt 6.2")) return "Windows 8";
        if (s.contains("windows nt 6.1")) return "Windows 7";
        if (s.contains("windows")) return "Windows";
        if (s.contains("mac os x")) return "macOS";
        if (s.contains("iphone")) return "iOS";
        if (s.contains("ipad")) return "iPadOS";
        if (s.contains("android")) return "Android";
        if (s.contains("linux")) return "Linux";
        return "Other";
    }
}

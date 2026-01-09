package cc.oofo.utils.context;

/**
 * 审计上下文（用于 created/updated 等字段填充）。
 *
 * 不依赖 Web/鉴权实现，由上层（Filter/Interceptor/任务执行器）负责写入。
 */
public final class AuditContext {

    private final String userId;
    private final String userName;

    public AuditContext(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }
}

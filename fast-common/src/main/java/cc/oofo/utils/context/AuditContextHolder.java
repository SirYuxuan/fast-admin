package cc.oofo.utils.context;

/**
 * 审计上下文持有者（ThreadLocal）。
 */
public final class AuditContextHolder {

    private static final ThreadLocal<AuditContext> CONTEXT = new ThreadLocal<>();

    private AuditContextHolder() {
    }

    public static void set(AuditContext context) {
        CONTEXT.set(context);
    }

    public static AuditContext get() {
        return CONTEXT.get();
    }

    public static String getUserIdOrDefault(String defaultUserId) {
        AuditContext ctx = CONTEXT.get();
        return (ctx == null || ctx.getUserId() == null || ctx.getUserId().isBlank()) ? defaultUserId : ctx.getUserId();
    }

    public static String getUserNameOrDefault(String defaultUserName) {
        AuditContext ctx = CONTEXT.get();
        return (ctx == null || ctx.getUserName() == null || ctx.getUserName().isBlank()) ? defaultUserName
                : ctx.getUserName();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

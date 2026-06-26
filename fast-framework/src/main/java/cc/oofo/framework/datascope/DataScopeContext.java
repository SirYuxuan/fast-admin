package cc.oofo.framework.datascope;

/**
 * 数据权限上下文——基于 ThreadLocal，由 {@code DataScopeAspect} 写入，
 * 目前作为扩展预留（当前方案通过 BaseQuery 字段传递范围条件）。
 *
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
public final class DataScopeContext {

    private static final ThreadLocal<DataScopeType> HOLDER = new ThreadLocal<>();

    private DataScopeContext() {
    }

    public static void set(DataScopeType type) {
        HOLDER.set(type);
    }

    public static DataScopeType get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}

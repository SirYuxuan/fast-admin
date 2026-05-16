package cc.oofo.system.log.enums;

/**
 * 业务类型
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
public enum BusinessType {
    /** 其它 */
    OTHER,
    /** 新增 */
    CREATE,
    /** 修改 */
    UPDATE,
    /** 删除 */
    DELETE,
    /** 查询 */
    QUERY,
    /** 授权 */
    GRANT,
    /** 导入 */
    IMPORT,
    /** 导出 */
    EXPORT,
    /** 强退 */
    FORCE_LOGOUT,
    /** 清空数据 */
    CLEAN
}

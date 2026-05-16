package cc.oofo.database.config;

import java.sql.Statement;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 慢 SQL 监控拦截器：执行耗时超过阈值则输出 WARN 日志。
 *
 * 配置项：
 *   fast.sql.slow-threshold-ms: 1000  (单位 ms，默认 1000)
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Slf4j
@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query",
                args = {Statement.class, org.apache.ibatis.session.ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update",
                args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch",
                args = {Statement.class})
})
public class SqlSlowLogInterceptor implements Interceptor {

    @Value("${fast.sql.slow-threshold-ms:1000}")
    private long thresholdMs;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost >= thresholdMs) {
                try {
                    StatementHandler handler = (StatementHandler) invocation.getTarget();
                    MetaObject meta = SystemMetaObject.forObject(handler);
                    MappedStatement ms = (MappedStatement) meta.getValue("delegate.mappedStatement");
                    BoundSql boundSql = handler.getBoundSql();
                    String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();
                    log.warn("⚠️ SLOW SQL [{} ms] {} -> {}",
                            cost, ms.getId(), trim(sql, 800));
                } catch (Exception e) {
                    log.warn("⚠️ SLOW SQL [{} ms] (sql extract failed: {})", cost, e.getMessage());
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return (target instanceof StatementHandler) ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties properties) {
    }

    private String trim(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...(truncated)";
    }
}

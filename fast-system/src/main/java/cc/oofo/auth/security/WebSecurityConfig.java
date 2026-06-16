package cc.oofo.auth.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import cc.oofo.auth.security.filter.AuditContextFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;

/**
 * Web 安全配置
 *
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;
    private final InterceptorProperties interceptorProperties;
    private final AuditContextFilter auditContextFilter;

    public WebSecurityConfig(CorsProperties corsProperties, InterceptorProperties interceptorProperties,
            AuditContextFilter auditContextFilter) {
        this.corsProperties = corsProperties;
        this.interceptorProperties = interceptorProperties;
        this.auditContextFilter = auditContextFilter;
    }

    /**
     * 处理跨域问题
     *
     * @return 跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        if (!corsProperties.isEnabled()) {
            return new CorsFilter(new UrlBasedCorsConfigurationSource());
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(corsProperties.isAllowCredentials());

        // 设置允许的域
        corsProperties.getAllowedOrigins().forEach(config::addAllowedOriginPattern);
        // 设置允许的请求头
        corsProperties.getAllowedHeaders().forEach(config::addAllowedHeader);
        // 设置允许的请求方法
        corsProperties.getAllowedMethods().forEach(config::addAllowedMethod);

        config.setMaxAge(corsProperties.getMaxAge());

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 注册拦截器
     */
    @Override
    @SuppressWarnings("null")
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        String[] includePatterns = interceptorProperties.getAuth().getIncludePatterns().toArray(new String[0]);
        String[] excludePatterns = interceptorProperties.getAuth().getExcludePatterns().toArray(new String[0]);
        // SSE 等异步请求在 emitter 完成后会再次分派进入拦截器链，此时 Sa-Token 上下文已释放，
        // 重复 checkLogin 会抛“上下文尚未初始化”。仅在初始 REQUEST 分派鉴权即可，异步/错误再分派放行。
        SaInterceptor saInterceptor = new SaInterceptor(handle -> StpUtil.checkLogin()) {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                if (request.getDispatcherType() != DispatcherType.REQUEST) {
                    return true;
                }
                return super.preHandle(request, response, handler);
            }
        };
        registry.addInterceptor(saInterceptor)
                .addPathPatterns(includePatterns)
                .excludePathPatterns(excludePatterns);

        // 将登录用户写入审计上下文，供持久层自动填充
        registry.addInterceptor(auditContextFilter)
                .addPathPatterns(includePatterns)
                .excludePathPatterns(excludePatterns);
    }

    /**
     * CORS 配置属性
     */
    @Data
    @Configuration
    @ConfigurationProperties(prefix = "cors")
    public static class CorsProperties {
        private boolean enabled = true;
        private List<String> allowedOrigins = List.of("*");
        private List<String> allowedHeaders = List.of("*");
        private List<String> allowedMethods = List.of("*");
        private boolean allowCredentials = true;
        private Long maxAge = 3600L;
    }

    /**
     * 拦截器配置属性
     */
    @Data
    @Configuration
    @ConfigurationProperties(prefix = "interceptor")
    public static class InterceptorProperties {
        private AuthInterceptor auth = new AuthInterceptor();

        @Data
        public static class AuthInterceptor {
            private List<String> includePatterns = List.of("/**");
            private List<String> excludePatterns = List.of("/auth/login");
        }
    }
}
package cc.oofo.framework.web.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Knife4j / OpenAPI 配置
 *
 * 访问地址：
 *   http://localhost:8080/doc.html        ← Knife4j 增强 UI
 *   http://localhost:8080/swagger-ui.html ← Swagger 原生 UI
 *   http://localhost:8080/v3/api-docs     ← OpenAPI JSON
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Configuration
public class Knife4jConfig {

    /** 全局配置（标题/描述/作者/版本/鉴权） */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fast Admin API")
                        .description("Fast 快速开发平台后端接口文档")
                        .version("v1.0.0")
                        .contact(new Contact().name("Sir丶雨轩").email("siryuxuan66@gmail.com"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                // 全局鉴权（Knife4j 顶部"Authorize"按钮填的就是这个）
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("Sa-Token 登录后返回的 token，直接填写即可")))
                .addSecurityItem(new SecurityRequirement().addList("token"));
    }

    /** 系统管理分组 */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("1.系统管理")
                .pathsToMatch("/system/**", "/auth/**")
                .build();
    }

    /** 业务示例分组 */
    @Bean
    public GroupedOpenApi bizApi() {
        return GroupedOpenApi.builder()
                .group("2.业务")
                .pathsToMatch("/biz/**")
                .build();
    }

    /** 全部接口 */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0.全部")
                .pathsToMatch("/**")
                .build();
    }
}

package cc.oofo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Fast应用启动类
 *
 * <p>Spring AI 相关自动配置（OpenAI / Anthropic / MCP 等）通过
 * {@code config/application-ai.yml} 里的 {@code spring.ai.model.*}、
 * {@code spring.ai.mcp.client.enabled} 开关关闭，因为模型配置在运行时
 * 从 {@code sys_config} 动态读取，无需启动期自动装配。</p>
 *
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@EnableAsync
@SpringBootApplication
public class FastApplication {

    public static void main(String[] args) {
        SpringApplication.run(FastApplication.class, args);
    }
}

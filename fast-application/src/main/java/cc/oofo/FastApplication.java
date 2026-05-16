package cc.oofo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Fast应用启动类
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

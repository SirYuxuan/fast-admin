package cc.oofo.framework.web.config;

import java.text.SimpleDateFormat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web 配置类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Configuration
public class WebConfig {

    /**
     * 配置 Jackson ObjectMapper
     * 全局设置日期时间格式，不包含毫秒
     */
    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
                .json()
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .build();
    }

}
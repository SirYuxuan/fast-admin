package cc.oofo.framework.web.config;

import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;

/**
 * Web 配置类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Configuration
public class WebConfig {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    /**
     * 配置 Jackson ObjectMapper
     * 全局设置日期时间格式，不包含毫秒
     */
    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
                .json()
                .timeZone(TimeZone.getTimeZone(DEFAULT_ZONE))
                .dateFormat(new SimpleDateFormat(DATE_TIME_PATTERN))
                .serializerByType(Date.class, new DateSerializer())
                .serializerByType(Timestamp.class, new TimestampSerializer())
                .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer())
                .build();
    }

    /**
     * Spring Boot 4 默认使用 Jackson 3，HTTP 响应时间格式在这里统一处理。
     */
    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("fast-date-time");
            module.addSerializer(Date.class, new Jackson3DateSerializer());
            module.addSerializer(Timestamp.class, new Jackson3TimestampSerializer());
            module.addSerializer(LocalDateTime.class, new Jackson3LocalDateTimeSerializer());
            builder.defaultTimeZone(TimeZone.getTimeZone(DEFAULT_ZONE))
                    .defaultDateFormat(new SimpleDateFormat(DATE_TIME_PATTERN))
                    .addModule(module);
        };
    }

    private static class DateSerializer extends StdSerializer<Date> {

        private DateSerializer() {
            super(Date.class);
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws java.io.IOException {
            gen.writeString(DATE_TIME_FORMATTER.format(value.toInstant().atZone(DEFAULT_ZONE).toLocalDateTime()));
        }
    }

    private static class TimestampSerializer extends StdSerializer<Timestamp> {

        private TimestampSerializer() {
            super(Timestamp.class);
        }

        @Override
        public void serialize(Timestamp value, JsonGenerator gen, SerializerProvider provider) throws java.io.IOException {
            gen.writeString(DATE_TIME_FORMATTER.format(value.toInstant().atZone(DEFAULT_ZONE).toLocalDateTime()));
        }
    }

    private static class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

        private LocalDateTimeSerializer() {
            super(LocalDateTime.class);
        }

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider)
                throws java.io.IOException {
            gen.writeString(DATE_TIME_FORMATTER.format(value));
        }
    }

    private static class Jackson3DateSerializer extends tools.jackson.databind.ser.std.StdSerializer<Date> {

        private Jackson3DateSerializer() {
            super(Date.class);
        }

        @Override
        public void serialize(Date value, tools.jackson.core.JsonGenerator gen, SerializationContext ctxt)
                throws tools.jackson.core.JacksonException {
            gen.writeString(DATE_TIME_FORMATTER.format(value.toInstant().atZone(DEFAULT_ZONE).toLocalDateTime()));
        }
    }

    private static class Jackson3TimestampSerializer extends tools.jackson.databind.ser.std.StdSerializer<Timestamp> {

        private Jackson3TimestampSerializer() {
            super(Timestamp.class);
        }

        @Override
        public void serialize(Timestamp value, tools.jackson.core.JsonGenerator gen, SerializationContext ctxt)
                throws tools.jackson.core.JacksonException {
            gen.writeString(DATE_TIME_FORMATTER.format(value.toInstant().atZone(DEFAULT_ZONE).toLocalDateTime()));
        }
    }

    private static class Jackson3LocalDateTimeSerializer
            extends tools.jackson.databind.ser.std.StdSerializer<LocalDateTime> {

        private Jackson3LocalDateTimeSerializer() {
            super(LocalDateTime.class);
        }

        @Override
        public void serialize(LocalDateTime value, tools.jackson.core.JsonGenerator gen, SerializationContext ctxt)
                throws tools.jackson.core.JacksonException {
            gen.writeString(DATE_TIME_FORMATTER.format(value));
        }
    }

}

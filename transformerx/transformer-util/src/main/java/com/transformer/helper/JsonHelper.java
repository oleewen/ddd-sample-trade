package com.transformer.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.transformer.exception.helper.ExceptionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Simple utility methods for working with the Jackson API.
 * <p>
 * 公共类库，修改或增加能力，请联系author
 * </p>
 *
 * @author ouliyuan 2023/06/30
 * @see ObjectMapper
 */
@Slf4j
public final class JsonHelper {

    private JsonHelper() {
    }

    private static final class InstanceHolder {
        // Singleton instance: 所有属性（无论属性值是否为空）
        private static final ObjectMapper INSTANCE_OF_INCLUDE_ALWAYS;
        private static final ObjectMapper INSTANCE_OF_CAMEL_ALL_PROPERTY;
        // Singleton instance: 非空属性
        private static final ObjectMapper INSTANCE_OF_INCLUDE_NON_EMPTY;

        private static final String STANDARD_PATTERN = "yyyy-MM-dd HH:mm:ss";
        private static final String DATE_PATTERN = "yyyy-MM-dd";
        private static final String TIME_PATTERN = "HH:mm:ss";

        static {
            // 初始化JavaTimeModule
            JavaTimeModule javaTimeModule = new JavaTimeModule();

            // 处理LocalDateTime
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(STANDARD_PATTERN);
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

            // 处理LocalDate
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

            // 处理LocalTime
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
            javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
            javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

            INSTANCE_OF_CAMEL_ALL_PROPERTY = new ObjectMapper()
                    // 包含对象的所有属性字段
                    .setSerializationInclusion(JsonInclude.Include.ALWAYS)
                    // 忽略空bean转json的错误
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    // 忽略未知属性字段
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    // 取消默认转换为timestamps的形式
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    // 所有的日期格式统一格式化为以下样式 yyyy-MM-dd HH:mm:ss
                    .setDateFormat(new SimpleDateFormat(STANDARD_PATTERN))
                    .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                    // 注册时间模块, 支持jsr310, 即新的时间类(java.time包下的时间类)
                    .registerModule(javaTimeModule);
            INSTANCE_OF_INCLUDE_ALWAYS = new ObjectMapper()
                    // 包含对象的所有属性字段
                    .setSerializationInclusion(JsonInclude.Include.ALWAYS)
                    // 忽略空bean转json的错误
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    // 忽略未知属性字段
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    // 取消默认转换为timestamps的形式
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    // 所有的日期格式统一格式化为以下样式 yyyy-MM-dd HH:mm:ss
                    .setDateFormat(new SimpleDateFormat(STANDARD_PATTERN))
                    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    // 注册时间模块, 支持jsr310, 即新的时间类(java.time包下的时间类)
                    .registerModule(javaTimeModule);

            INSTANCE_OF_INCLUDE_NON_EMPTY = new ObjectMapper()
                    // 包含对象的非空属性字段
                    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                    // 忽略空bean转json的错误
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    // 忽略未知属性字段
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    // 取消默认转换为timestamps的形式
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    // 所有的日期格式统一格式化为以下样式 yyyy-MM-dd HH:mm:ss
                    .setDateFormat(new SimpleDateFormat(STANDARD_PATTERN))
                    // 注册时间模块, 支持jsr310, 即新的时间类(java.time包下的时间类)
                    .registerModule(javaTimeModule);
        }
    }

    private static ObjectMapper instanceOfAll() {
        return InstanceHolder.INSTANCE_OF_INCLUDE_ALWAYS;
    }

    private static ObjectMapper instanceOfNonEmpty() {
        return InstanceHolder.INSTANCE_OF_INCLUDE_NON_EMPTY;
    }

    public static <T> String toJson(T value) {
        try {
            return instanceOfAll().writeValueAsString(value);
        } catch (Exception e) {
            throw ExceptionHelper.createThrowableRuntimeException(e);
        }
    }

    public static <T> String toPrettyJson(T value) {
        try {
            return instanceOfAll().writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception e) {
            throw ExceptionHelper.createThrowableRuntimeException(e);
        }
    }

    public static <T> String toJsonNotEmpty(T value) {
        try {
            return instanceOfNonEmpty().writeValueAsString(value);
        } catch (Exception e) {
            throw ExceptionHelper.createThrowableRuntimeException(e);
        }
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json) || Objects.isNull(clazz)) {
            return null;
        }

        try {
            return instanceOfAll().readValue(json, clazz);
        } catch (Exception e) {
            log.error("parse " + json + " to " + clazz + " class exception", e);
            return null;
        }
    }

    public static <T> T parseObject(String json, TypeReference<T> type) {
        if (StringUtils.isBlank(json) || Objects.isNull(type)) {
            return null;
        }

        try {
            return instanceOfAll().readValue(json, type);
        } catch (Exception e) {
            log.error("parse " + json + " to " + type + " type exception", e);
            return null;
        }
    }

    public static <T> T parseObject(InputStream src, Class<T> clazz) {
        if (Objects.isNull(src) || Objects.isNull(clazz)) {
            return null;
        }

        try {
            return instanceOfAll().readValue(src, clazz);
        } catch (Exception e) {
            log.error("parse stream to " + clazz + " exception", e);
            return null;
        }
    }

    public static <T> List<T> parseList(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json) || Objects.isNull(clazz)) {
            return Collections.emptyList();
        }

        JavaType javaType = instanceOfAll().getTypeFactory().constructCollectionType(List.class, clazz);
        try {
            return instanceOfAll().readValue(json, javaType);
        } catch (Exception e) {
            log.error("parseList:exception", e);
            return Collections.emptyList();
        }
    }

    public static <T> Set<T> parseSet(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json) || Objects.isNull(clazz)) {
            return Collections.emptySet();
        }

        JavaType javaType = instanceOfAll().getTypeFactory().constructCollectionType(Set.class, clazz);
        try {
            return instanceOfAll().readValue(json, javaType);
        } catch (Exception e) {
            log.error("parseSet:exception", e);
            return Collections.emptySet();
        }
    }

    public static <T> T parseAllObject(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json) || Objects.isNull(clazz)) {
            return null;
        }

        try {
            return instanceOfCamelAll().readValue(json, clazz);
        } catch (Exception e) {
            log.error("parseAllObject:exception", e);
            return null;
        }
    }

    private static ObjectMapper instanceOfCamelAll() {
        return InstanceHolder.INSTANCE_OF_CAMEL_ALL_PROPERTY;
    }

    public static <K, V> Map<K, V> parseMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (StringUtils.isBlank(json) || Objects.isNull(keyClass) || Objects.isNull(valueClass)) {
            return Collections.emptyMap();
        }

        JavaType javaType = instanceOfAll().getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
        try {
            return instanceOfAll().readValue(json, javaType);
        } catch (Exception e) {
            log.error("parseMap:exception", e);
            return Collections.emptyMap();
        }
    }

    public static Map<String, Object> parseMap(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }

        MapLikeType mapLikeType = instanceOfAll().getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class);
        try {
            return instanceOfAll().readValue(json, mapLikeType);
        } catch (Exception e) {
            log.error("parseMap:exception", e);
            return Collections.emptyMap();
        }
    }
}

package com.sky.demo.web_demo_multi_tenant_separate_schema.util.json;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by rg on 9/13/16.
 */
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
//        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); //属性为NULL不序列化, 注意：只对VO起作用，Map List不起作用
    }

    /**
     * 对象序列化为json字符串
     * @param object
     * @return
     */
    public static String writeValueAsString(Object object) {
        try {
            String result = objectMapper.writeValueAsString(object);
            if (StringUtils.equals(result, "null")) {
                result = "";
            }

            return result;
        } catch (JsonProcessingException e) {
            logger.error("write value error", e);
            throw new JsonOperationException(e);
        }
    }

    /**
     * json字符串反序列化为对象
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T readValue(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            logger.error("read value error", e);
            throw new JsonOperationException(e);
        }
    }

    /**
     * json字符串反序列化为对象
     * @param json
     * @param javaType
     * @param <T>
     * @return
     */
    public static <T> T readValue(String json, JavaType javaType) {
        try {
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            logger.error("read value error", e);
            throw new JsonOperationException(e);
        }
    }

    public static <T> T readValue(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            logger.error("read value error", e);
            throw new JsonOperationException(e);
        }
    }

    public static <T> T readValue(String json, Class<?> parametrized, Class<?> parametersFor, Class<?>... parameterClasses) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructParametrizedType(parametrized, parametersFor, parameterClasses);
            return objectMapper.readValue(json, javaType);
        } catch (Exception e) {
            logger.error("read value error", e);
            throw new JsonOperationException(e);
        }
    }
}

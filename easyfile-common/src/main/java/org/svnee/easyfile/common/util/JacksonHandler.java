package org.svnee.easyfile.common.util;

import static org.svnee.easyfile.common.util.ExceptionUtils.rethrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * JsonUtils
 *
 * @author svnee
 **/
public final class JacksonHandler implements JsonFacade {

    private final ObjectMapper mapper = newMapper();

    /**
     * 基于默认配置, 创建一个新{@link ObjectMapper},
     * 随后可以定制化这个新{@link ObjectMapper}.
     */
    public ObjectMapper newMapper() {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        mapper.setDateFormat(dateFormat);
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        return mapper;
    }

    /**
     * 获取默认{@link ObjectMapper}.
     * 直接使用默认{@link ObjectMapper}时需要小心,
     * 因为{@link ObjectMapper}类是可变的,
     * 对默认 ObjectMapper 的改动会影响所有默认ObjectMapper的依赖方.
     * 如果需要在当前上下文定制化{@link ObjectMapper},
     * 建议使用{@link #newMapper()}方法创建一个新的{@link ObjectMapper}.
     *
     * @see #newMapper()
     */
    public ObjectMapper mapper() {
        return mapper;
    }

    @Override
    public String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return rethrow(e);
        }
    }

    @Override
    public <T> T parseObject(String text, Class<T> clazz) {
        try {
            return mapper.readValue(text, clazz);
        } catch (IOException e) {
            return rethrow(e);
        }
    }

    @Override
    public <T> T parseObject(String text, TypeReference<T> typeReference) {
        try {
            Type type = typeReference.getType();
            JavaType javaType = mapper.getTypeFactory().constructType(type);
            return mapper.readValue(text, javaType);
        } catch (IOException e) {
            return rethrow(e);
        }
    }

    @Override
    public <T> List<T> parseArray(String text, Class<T> clazz) {
        return parseCollection(text, List.class, clazz);
    }

    @Override
    public <T> Set<T> parseSet(String json, Class<T> type) {
        return parseCollection(json, Set.class, type);
    }

    private <V, C extends Collection<?>, T> V parseCollection(String json, Class<C> collectionType,
        Class<T> elementType) {
        try {
            TypeFactory typeFactory = mapper.getTypeFactory();
            CollectionType javaType = typeFactory.constructCollectionType(collectionType, elementType);
            return mapper.readValue(json, javaType);
        } catch (IOException e) {
            return rethrow(e);
        }
    }
}

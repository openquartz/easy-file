package com.openquartz.easyfile.common.serdes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.openquartz.easyfile.common.dictionary.BaseEnum;
import com.openquartz.easyfile.common.util.StringUtils;
import java.io.IOException;
import java.util.Objects;
import lombok.Data;

/**
 * BaseEnum反序列化
 *
 * @author svnee
 */
@Data
public class BaseEnumDeserializer extends JsonDeserializer<BaseEnum<?>> implements ContextualDeserializer {

    private Class<?> clz;

    @Override
    public BaseEnum<?> deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        if (StringUtils.isEmpty(jsonParser.getText())) {
            return null;
        }
        if (clz.isEnum() && BaseEnum.class.isAssignableFrom(clz)) {
            BaseEnum<?>[] constants = (BaseEnum<?>[]) clz.getEnumConstants();
            String valueAsString = jsonParser.getValueAsString();
            if (Objects.nonNull(constants)) {
                for (BaseEnum<?> item : constants) {
                    if (String.valueOf(item.getCode()).equals(valueAsString)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取合适的解析器，把当前解析的属性Class对象存起来，以便反序列化的转换类型;
     *
     * @param ctx ctx
     * @param property pro
     * @return JsonDeserializer
     */
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) {
        Class<?> rawCls = ctx.getContextualType().getRawClass();
        BaseEnumDeserializer clone = new BaseEnumDeserializer();
        clone.setClz(rawCls);
        return clone;
    }
}
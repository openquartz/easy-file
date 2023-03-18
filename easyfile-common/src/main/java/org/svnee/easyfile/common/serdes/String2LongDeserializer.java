package org.svnee.easyfile.common.serdes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;
import lombok.Data;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * String 2 Long
 *
 * @author svnee
 */
@Data
public class String2LongDeserializer extends JsonDeserializer<Long> implements ContextualDeserializer {

    private Class<?> clz;

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        if (StringUtils.isEmpty(jsonParser.getText())) {
            return null;
        }
        String valueAsString = jsonParser.getValueAsString();
        return Long.parseLong(valueAsString.trim());
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
        String2LongDeserializer clone = new String2LongDeserializer();
        clone.setClz(rawCls);
        return clone;
    }
}
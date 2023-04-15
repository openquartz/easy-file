package com.openquartz.easyfile.common.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.openquartz.easyfile.common.dictionary.BaseEnum;
import java.io.IOException;

/**
 * 基础枚举序列化
 *
 * @author svnee
 */
public class BaseEnumSerializer extends StdSerializer<BaseEnum> {

    protected BaseEnumSerializer() {
        super(BaseEnum.class);
    }

    @Override
    public void serialize(BaseEnum value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(String.valueOf(value.getCode()));
    }
}

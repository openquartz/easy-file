package org.svnee.easyfile.common.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.svnee.easyfile.common.dictionary.BaseEnum;

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

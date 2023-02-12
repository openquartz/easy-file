package org.svnee.easyfile.common.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Objects;

/**
 * Long2String
 * @author svnee
 **/
public class Long2StringSerializer extends StdSerializer<Long> {

    protected Long2StringSerializer(Class<Long> t) {
        super(t);
    }

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (Objects.nonNull(value)) {
            gen.writeString(String.valueOf(value));
        }
    }
}

package org.svnee.easyfile.common.util.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.svnee.easyfile.common.util.json.JacksonHandler;

/**
 * general jackson handler
 *
 * @author svnee
 **/
public class GeneralJacksonHandler extends JacksonHandler {

    @Override
    public ObjectMapper newMapper() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(dateFormat);
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        return objectMapper;
    }
}

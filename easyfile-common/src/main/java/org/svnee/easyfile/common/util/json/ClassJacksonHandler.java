package org.svnee.easyfile.common.util.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.svnee.easyfile.common.util.json.GeneralJacksonHandler;

/**
 * Class Jackson handler
 *
 * @author svnee
 **/
public class ClassJacksonHandler extends GeneralJacksonHandler {

    @Override
    public ObjectMapper newMapper() {
        ObjectMapper objectMapper = super.newMapper();
        objectMapper.activateDefaultTypingAsProperty(LaissezFaireSubTypeValidator.instance,
            DefaultTyping.NON_FINAL, "@class");
        return objectMapper;
    }
}

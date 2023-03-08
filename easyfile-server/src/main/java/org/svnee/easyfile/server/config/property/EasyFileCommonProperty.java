package org.svnee.easyfile.server.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.svnee.easyfile.common.property.IEasyFileCommonProperties;

/**
 * @author svnee
 **/
@Data
@ConfigurationProperties(prefix = EasyFileCommonProperty.PREFIX)
public class EasyFileCommonProperty implements IEasyFileCommonProperties {

    public static final String PREFIX = "easyfile.common";

    private String appId;
    private String unifiedAppId;

}

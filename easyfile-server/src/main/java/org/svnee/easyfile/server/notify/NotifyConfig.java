package org.svnee.easyfile.server.notify;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * @author svnee
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "easyfile.server.notify")
public class NotifyConfig {

    /**
     * 是否开启
     */
    private boolean enabled = false;

    /**
     * 通知方式
     */
    private String ways;

    /**
     * Accessor Info
     */
    private Map<String, AccessorInfo> access;

    @Data
    public static class AccessorInfo {

        /**
         * token
         */
        private String token;

    }

    public List<NotifyWay> getWays() {
        if (StringUtils.isBlank(ways)) {
            return Collections.emptyList();
        }
        return Stream.of(ways.split(Constants.COMMA_SPLITTER))
            .map(NotifyWay::of)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}


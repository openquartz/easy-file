package com.openquartz.easyfile.starter.spring.boot.autoconfig.condition;

import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileImportProperties;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ConfigurationCondition;

/**
 * Match when download or import is enabled.
 *
 * @author svnee
 */
public class DownloadOrImportEnabledCondition extends AnyNestedCondition {

    public DownloadOrImportEnabledCondition() {
        super(ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
    static class DownloadEnabled {
    }

    @ConditionalOnProperty(prefix = EasyFileImportProperties.PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
    static class ImportEnabled {
    }
}

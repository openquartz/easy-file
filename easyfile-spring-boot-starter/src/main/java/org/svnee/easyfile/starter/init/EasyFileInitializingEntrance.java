package org.svnee.easyfile.starter.init;

import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.core.banner.EasyFileBannerPrinter;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;

/**
 * EasyFileInit Entrance
 *
 * @author svnee
 **/
@Slf4j
public class EasyFileInitializingEntrance {

    private final EasyFileDownloadProperties properties;

    public EasyFileInitializingEntrance(
        EasyFileDownloadProperties properties) {
        this.properties = properties;
    }

    /**
     * init method
     */
    public void init() {

        // print banner
        new EasyFileBannerPrinter().print(log);
    }

    /**
     * destroy method
     */
    public void destroy() {

    }

}

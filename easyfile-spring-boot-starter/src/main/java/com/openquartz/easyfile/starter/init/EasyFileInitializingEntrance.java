package com.openquartz.easyfile.starter.init;

import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.core.banner.EasyFileBannerPrinter;

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

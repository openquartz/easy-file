package com.openquartz.easyfile.starter.init;

import com.openquartz.easyfile.core.banner.EasyFileBannerPrinter;
import lombok.extern.slf4j.Slf4j;

/**
 * EasyFileInit Entrance
 *
 * @author svnee
 **/
@Slf4j
public class EasyFileInitializingEntrance {

    public EasyFileInitializingEntrance() {
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

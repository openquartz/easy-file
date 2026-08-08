package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * EasyFileImportProperties unit tests.
 */
public class EasyFileImportPropertiesTest {

    @Test
    public void defaultTriggerType_isThreadPool() {
        Assert.assertEquals(ImportAsyncTriggerType.THREAD_POOL, new EasyFileImportProperties().getTriggerType());
    }
}

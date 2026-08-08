package com.openquartz.easyfile.storage.local.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.storage.expand.ExportLimitingExecutor;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadTaskMapper;
import java.util.Collections;
import org.junit.Test;

/**
 * Registration assert for duplicate limiting strategies.
 */
public class LocalLimitingServiceImplTest {

    @Test
    public void postProcessShouldAllowFirstRegistration() {
        LocalLimitingServiceImpl service = new LocalLimitingServiceImpl(
            (AsyncDownloadTaskMapper) null, Collections.emptyList());
        Object bean = service.postProcessAfterInitialization(new DummyExecutor("s1"), "e1");
        assertNotNull(bean);
    }

    @Test
    public void postProcessShouldAllowSameInstanceReregistration() {
        DummyExecutor executor = new DummyExecutor("s2");
        LocalLimitingServiceImpl service = new LocalLimitingServiceImpl(
            (AsyncDownloadTaskMapper) null, Collections.singletonList(executor));
        Object bean = service.postProcessAfterInitialization(executor, "e2");
        assertNotNull(bean);
    }

    @Test
    public void postProcessShouldRejectDifferentInstanceSameStrategy() {
        DummyExecutor first = new DummyExecutor("s3");
        LocalLimitingServiceImpl service = new LocalLimitingServiceImpl(
            (AsyncDownloadTaskMapper) null, Collections.singletonList(first));
        try {
            service.postProcessAfterInitialization(new DummyExecutor("s3"), "e3");
            fail("expected EasyFileException for duplicate strategy");
        } catch (EasyFileException ex) {
            assertNotNull(ex.getErrorCode());
        }
    }

    private static class DummyExecutor implements ExportLimitingExecutor {
        private final String strategy;

        private DummyExecutor(String strategy) {
            this.strategy = strategy;
        }

        @Override
        public String strategy() {
            return strategy;
        }

        @Override
        public void limit(ExportLimitingRequest request) {
            // no-op
        }
    }
}

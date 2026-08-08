package com.openquartz.easyfile.storage.local.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.common.request.ImportLimitingRequest;
import com.openquartz.easyfile.storage.expand.ExportLimitingExecutor;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadTaskMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportRecordMapper;
import com.openquartz.easyfile.storage.local.mapper.condition.ImportRecordQueryCondition;
import java.util.Collections;
import org.junit.Test;

/**
 * Registration assert for duplicate limiting strategies + import concurrent reject.
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

    @Test
    public void importLimiting_rejectsWhenInFlightAtMax() {
        AsyncImportRecordMapper recordMapper = mock(AsyncImportRecordMapper.class);
        when(recordMapper.countByCondition(any(ImportRecordQueryCondition.class))).thenReturn(5, 5);

        LocalLimitingServiceImpl service = new LocalLimitingServiceImpl(
            null, Collections.emptyList(), recordMapper, 10);

        ImportLimitingRequest request = new ImportLimitingRequest();
        request.setAppId("app");
        request.setImportCode("student-import");

        try {
            service.limiting(request);
            fail("expected EasyFileException when in-flight >= max");
        } catch (EasyFileException ex) {
            assertNotNull(ex.getErrorCode());
        }
    }

    @Test
    public void importLimiting_allowsWhenUnderMax() {
        AsyncImportRecordMapper recordMapper = mock(AsyncImportRecordMapper.class);
        when(recordMapper.countByCondition(any(ImportRecordQueryCondition.class))).thenReturn(3, 2);

        LocalLimitingServiceImpl service = new LocalLimitingServiceImpl(
            null, Collections.emptyList(), recordMapper, 10);

        ImportLimitingRequest request = new ImportLimitingRequest();
        request.setAppId("app");
        request.setImportCode("student-import");
        service.limiting(request);
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

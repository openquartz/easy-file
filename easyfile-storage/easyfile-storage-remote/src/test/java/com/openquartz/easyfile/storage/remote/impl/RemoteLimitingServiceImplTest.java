package com.openquartz.easyfile.storage.remote.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.common.request.ImportLimitingRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.storage.remote.EasyFileClient;
import org.junit.Before;
import org.junit.Test;

/**
 * Remote limiting must fail closed on null/unsuccessful responses.
 */
public class RemoteLimitingServiceImplTest {

    private EasyFileClient easyFileClient;
    private RemoteLimitingServiceImpl limitingService;

    @Before
    public void setUp() {
        easyFileClient = mock(EasyFileClient.class);
        limitingService = new RemoteLimitingServiceImpl(easyFileClient, 10);
    }

    @Test
    public void shouldPassWhenResponseSuccess() {
        when(easyFileClient.limiting(any(ExportLimitingRequest.class))).thenReturn(ResponseResult.ok());
        limitingService.limiting(new ExportLimitingRequest());
    }

    @Test
    public void shouldFailWhenResponseNull() {
        when(easyFileClient.limiting(any(ExportLimitingRequest.class))).thenReturn(null);
        try {
            limitingService.limiting(new ExportLimitingRequest());
            fail("expected EasyFileException");
        } catch (EasyFileException expected) {
            // ok
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void shouldFailWhenResponseNotSuccess() {
        ResponseResult failed = ResponseResult.fail("err", "limit failed");
        when(easyFileClient.limiting(any(ExportLimitingRequest.class))).thenReturn(failed);
        try {
            limitingService.limiting(new ExportLimitingRequest());
            fail("expected EasyFileException");
        } catch (EasyFileException expected) {
            // ok
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void importLimiting_rejectsWhenInFlightAtMax() {
        Pagination<ImportResult> page = new Pagination<>();
        page.setTotalRecords(5L);
        ResponseResult ok = ResponseResult.ok(page);
        when(easyFileClient.listImportResult(any(ImportResultQueryRequest.class))).thenReturn(ok);

        ImportLimitingRequest request = new ImportLimitingRequest();
        request.setAppId("app");
        request.setImportCode("code");
        try {
            limitingService.limiting(request);
            fail("expected EasyFileException");
        } catch (EasyFileException expected) {
            assertNotNull(expected.getErrorCode());
        }
    }
}

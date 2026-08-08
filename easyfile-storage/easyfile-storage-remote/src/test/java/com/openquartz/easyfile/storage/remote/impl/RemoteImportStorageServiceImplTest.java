package com.openquartz.easyfile.storage.remote.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.EnableRunningRequest;
import com.openquartz.easyfile.storage.remote.EasyFileClient;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

/**
 * enableRunning must not NPE when data is null (mirror download remote).
 * autoRegisterTask must hit import-specific client path (not download task autoRegister).
 */
public class RemoteImportStorageServiceImplTest {

    private EasyFileClient easyFileClient;
    private RemoteImportStorageServiceImpl service;

    @Before
    public void setUp() {
        easyFileClient = mock(EasyFileClient.class);
        service = new RemoteImportStorageServiceImpl(easyFileClient);
    }

    @Test
    public void shouldReturnFalseWhenDataNull() {
        ResponseResult<Boolean> result = ResponseResult.ok(null);
        when(easyFileClient.enableImportRunning(any(EnableRunningRequest.class))).thenReturn(result);
        assertFalse(service.enableRunning(1L));
    }

    @Test
    public void shouldReturnTrueWhenDataTrue() {
        when(easyFileClient.enableImportRunning(any(EnableRunningRequest.class)))
            .thenReturn(ResponseResult.ok(Boolean.TRUE));
        assertTrue(service.enableRunning(2L));
    }

    @Test
    public void shouldReturnFalseWhenDataFalse() {
        when(easyFileClient.enableImportRunning(any(EnableRunningRequest.class)))
            .thenReturn(ResponseResult.ok(Boolean.FALSE));
        assertFalse(service.enableRunning(3L));
    }

    @Test
    public void shouldReturnFalseWhenResponseNull() {
        when(easyFileClient.enableImportRunning(any(EnableRunningRequest.class))).thenReturn(null);
        assertFalse(service.enableRunning(4L));
    }

    @Test
    public void autoRegisterTask_usesImportAutoRegisterClient() {
        AutoTaskRegisterRequest request = new AutoTaskRegisterRequest()
            .setAppId("app")
            .setUnifiedAppId("u-app")
            .setImportCodeMap(Collections.singletonMap("import-code", "import desc"));

        service.autoRegisterTask(request);

        verify(easyFileClient).autoRegisterImportTask(request);
        verify(easyFileClient, never()).autoRegisterTask(any(AutoTaskRegisterRequest.class));
    }
}

package com.openquartz.easyfile.storage.remote.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.EnableRunningRequest;
import com.openquartz.easyfile.storage.remote.EasyFileClient;
import org.junit.Before;
import org.junit.Test;

/**
 * enableRunning must not NPE when data is null.
 */
public class RemoteDownloadStorageServiceImplEnableRunningTest {

    private EasyFileClient easyFileClient;
    private RemoteDownloadStorageServiceImpl service;

    @Before
    public void setUp() {
        easyFileClient = mock(EasyFileClient.class);
        service = new RemoteDownloadStorageServiceImpl(easyFileClient);
    }

    @Test
    public void shouldReturnFalseWhenDataNull() {
        ResponseResult<Boolean> result = ResponseResult.ok(null);
        when(easyFileClient.enableRunning(any(EnableRunningRequest.class))).thenReturn(result);
        assertFalse(service.enableRunning(1L));
    }

    @Test
    public void shouldReturnTrueWhenDataTrue() {
        when(easyFileClient.enableRunning(any(EnableRunningRequest.class)))
            .thenReturn(ResponseResult.ok(Boolean.TRUE));
        assertTrue(service.enableRunning(2L));
    }

    @Test
    public void shouldReturnFalseWhenDataFalse() {
        when(easyFileClient.enableRunning(any(EnableRunningRequest.class)))
            .thenReturn(ResponseResult.ok(Boolean.FALSE));
        assertFalse(service.enableRunning(3L));
    }

    @Test
    public void shouldReturnFalseWhenResponseNull() {
        when(easyFileClient.enableRunning(any(EnableRunningRequest.class))).thenReturn(null);
        assertFalse(service.enableRunning(4L));
    }
}

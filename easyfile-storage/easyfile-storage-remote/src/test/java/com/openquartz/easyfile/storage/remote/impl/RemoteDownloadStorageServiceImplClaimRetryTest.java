package com.openquartz.easyfile.storage.remote.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.ListRetryableRequest;
import com.openquartz.easyfile.common.request.RetryDownloadRequest;
import com.openquartz.easyfile.storage.remote.EasyFileClient;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoteDownloadStorageServiceImplClaimRetryTest {

    @Mock
    private EasyFileClient easyFileClient;
    @InjectMocks
    private RemoteDownloadStorageServiceImpl service;

    @Test
    public void claimRetry_falseWhenResultNull() {
        when(easyFileClient.claimRetry(any(RetryDownloadRequest.class))).thenReturn(null);
        assertFalse(service.claimRetry(1L, "system"));
    }

    @Test
    public void claimRetry_falseWhenDataNull() {
        ResponseResult<Boolean> result = ResponseResult.ok();
        when(easyFileClient.claimRetry(any(RetryDownloadRequest.class))).thenReturn(result);
        assertFalse(service.claimRetry(2L, "system"));
    }

    @Test
    public void claimRetry_trueWhenDataTrue() {
        when(easyFileClient.claimRetry(any(RetryDownloadRequest.class))).thenReturn(ResponseResult.ok(Boolean.TRUE));
        assertTrue(service.claimRetry(3L, "admin-user"));
        verify(easyFileClient).claimRetry(argThat(req ->
            Long.valueOf(3L).equals(req.getRegisterId()) && "admin-user".equals(req.getOperator())));
    }

    @Test
    public void listRetryable_emptyWhenDataNull() {
        when(easyFileClient.listRetryable(any(ListRetryableRequest.class))).thenReturn(ResponseResult.ok());
        assertTrue(service.listRetryable(24, 50, 30).isEmpty());
        verify(easyFileClient).listRetryable(argThat(req -> req.getAppId() == null));
    }

    @Test
    public void listRetryable_returnsData() {
        when(easyFileClient.listRetryable(any(ListRetryableRequest.class)))
            .thenReturn(ResponseResult.ok(Arrays.asList(8L, 9L)));
        assertTrue(service.listRetryable(24, 50, 30).containsAll(Arrays.asList(8L, 9L)));
    }

    @Test
    public void listRetryable_fourArgSetsAppId() {
        when(easyFileClient.listRetryable(any(ListRetryableRequest.class)))
            .thenReturn(ResponseResult.ok(Arrays.asList(1L)));
        service.listRetryable(24, 50, 30, "app-x");
        verify(easyFileClient).listRetryable(argThat(req -> "app-x".equals(req.getAppId())));
    }
}

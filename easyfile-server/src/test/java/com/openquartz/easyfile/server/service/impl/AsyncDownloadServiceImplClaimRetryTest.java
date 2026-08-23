package com.openquartz.easyfile.server.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.server.config.BizConfig;
import com.openquartz.easyfile.server.mapper.AsyncDownloadRecordMapper;
import com.openquartz.easyfile.server.mapper.AsyncDownloadTaskMapper;
import com.openquartz.easyfile.server.service.NotifyService;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncDownloadServiceImplClaimRetryTest {

    @Mock
    private AsyncDownloadTaskMapper asyncDownloadTaskMapper;
    @Mock
    private AsyncDownloadRecordMapper asyncDownloadRecordMapper;
    @Mock
    private NotifyService notifyService;
    @Mock
    private BizConfig bizConfig;
    @InjectMocks
    private AsyncDownloadServiceImpl asyncDownloadService;

    @Test
    public void claimRetry_trueWhenOneRowUpdated() {
        when(asyncDownloadRecordMapper.claimRetry(3L, "system")).thenReturn(1);
        assertTrue(asyncDownloadService.claimRetry(3L, "system"));
    }

    @Test
    public void claimRetry_falseWhenNoRow() {
        when(asyncDownloadRecordMapper.claimRetry(3L, "system")).thenReturn(0);
        assertFalse(asyncDownloadService.claimRetry(3L, "system"));
    }

    @Test
    public void listRetryable_delegatesToMapper() {
        when(asyncDownloadRecordMapper.selectRetryableRegisterIds(any(), any(), eq(20), isNull()))
            .thenReturn(Collections.singletonList(7L));
        assertEquals(Collections.singletonList(7L), asyncDownloadService.listRetryable(24, 20, 30));
        verify(asyncDownloadRecordMapper).selectRetryableRegisterIds(any(), any(), eq(20), isNull());
    }

    @Test
    public void listRetryable_fourArgPassesAppId() {
        when(asyncDownloadRecordMapper.selectRetryableRegisterIds(any(), any(), eq(20), eq("app-b")))
            .thenReturn(Collections.singletonList(8L));
        assertEquals(Collections.singletonList(8L), asyncDownloadService.listRetryable(24, 20, 30, "app-b"));
        verify(asyncDownloadRecordMapper).selectRetryableRegisterIds(any(), any(), eq(20), eq("app-b"));
    }
}

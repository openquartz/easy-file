package com.openquartz.easyfile.storage.local.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadRecordMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadTaskMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalDownloadStorageServiceImplClaimRetryTest {

    @Mock
    private AsyncDownloadTaskMapper asyncDownloadTaskMapper;
    @Mock
    private AsyncDownloadRecordMapper asyncDownloadRecordMapper;
    @InjectMocks
    private LocalDownloadStorageServiceImpl service;

    @Test
    public void claimRetry_trueWhenMapperUpdatesOneRow() {
        when(asyncDownloadRecordMapper.claimRetry(9L, "op")).thenReturn(1);
        assertTrue(service.claimRetry(9L, "op"));
        assertTrue(service.retry(9L, "op"));
    }

    @Test
    public void claimRetry_falseWhenMapperUpdatesZeroRows() {
        when(asyncDownloadRecordMapper.claimRetry(9L, "op")).thenReturn(0);
        assertFalse(service.claimRetry(9L, "op"));
    }

    @Test
    public void listRetryable_mapsMapperIds() {
        when(asyncDownloadRecordMapper.selectRetryableRegisterIds(any(), any(), eq(50)))
            .thenReturn(Arrays.asList(1L, 2L));
        List<Long> ids = service.listRetryable(24, 50, 30);
        assertEquals(Arrays.asList(1L, 2L), ids);
        verify(asyncDownloadRecordMapper).selectRetryableRegisterIds(any(), any(), eq(50));
    }

    @Test
    public void listRetryable_emptyWhenMapperEmpty() {
        when(asyncDownloadRecordMapper.selectRetryableRegisterIds(any(), any(), eq(10)))
            .thenReturn(Collections.emptyList());
        assertTrue(service.listRetryable(2, 10, 30).isEmpty());
        verifyNoInteractions(asyncDownloadTaskMapper);
    }
}

package com.openquartz.easyfile.storage.local.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.request.DownloadTriggerRequest;
import com.openquartz.easyfile.storage.local.entity.AsyncDownloadTrigger;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadTriggerMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalDownloadTriggerServiceImplRetriggerTest {

    @Mock
    private AsyncDownloadTriggerMapper asyncDownloadTriggerMapper;
    @InjectMocks
    private LocalDownloadTriggerServiceImpl service;

    @Test
    public void retrigger_resetsExistingRowAndDoesNotInsert() {
        AsyncDownloadTrigger existing = new AsyncDownloadTrigger();
        existing.setRegisterId(5L);
        when(asyncDownloadTriggerMapper.selectByRegisterId(5L)).thenReturn(existing);
        when(asyncDownloadTriggerMapper.resetForRetry(5L)).thenReturn(1);

        service.retrigger(5L);

        verify(asyncDownloadTriggerMapper).resetForRetry(5L);
        verify(asyncDownloadTriggerMapper, never()).insert(any());
    }

    @Test
    public void retrigger_insertsWhenMissing() {
        when(asyncDownloadTriggerMapper.selectByRegisterId(6L)).thenReturn(null);

        service.retrigger(6L);

        verify(asyncDownloadTriggerMapper, never()).resetForRetry(6L);
        verify(asyncDownloadTriggerMapper).insert(any(AsyncDownloadTrigger.class));
    }

    @Test
    public void trigger_stillSkipsWhenRowExists() {
        AsyncDownloadTrigger existing = new AsyncDownloadTrigger();
        when(asyncDownloadTriggerMapper.selectByRegisterId(7L)).thenReturn(existing);
        DownloadTriggerRequest request = new DownloadTriggerRequest();
        request.setRegisterId(7L);

        service.trigger(request);

        verify(asyncDownloadTriggerMapper, never()).insert(any());
        verify(asyncDownloadTriggerMapper, never()).resetForRetry(7L);
    }
}

package com.openquartz.easyfile.storage.local.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.storage.local.dictionary.ImportTriggerStatusEnum;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportTriggerMapper;
import org.junit.Before;
import org.junit.Test;

/**
 * LocalImportTriggerServiceImpl unit tests with mocked mapper.
 */
public class LocalImportTriggerServiceImplTest {

    private AsyncImportTriggerMapper asyncImportTriggerMapper;
    private LocalImportTriggerServiceImpl service;

    @Before
    public void setUp() {
        asyncImportTriggerMapper = mock(AsyncImportTriggerMapper.class);
        service = new LocalImportTriggerServiceImpl(asyncImportTriggerMapper);
    }

    @Test
    public void startExecute_transitionsToExecuting() {
        Long registerId = 100L;
        Integer triggerCount = 0;

        when(asyncImportTriggerMapper.execute(
            eq(registerId),
            eq(ImportTriggerStatusEnum.EXECUTING),
            anyList(),
            eq(triggerCount)))
            .thenReturn(1);

        assertTrue(service.startExecute(registerId, triggerCount));
    }

    @Test
    public void startExecute_returnsFalseWhenCasMisses() {
        Long registerId = 101L;
        Integer triggerCount = 1;

        when(asyncImportTriggerMapper.execute(
            eq(registerId),
            eq(ImportTriggerStatusEnum.EXECUTING),
            anyList(),
            eq(triggerCount)))
            .thenReturn(0);

        assertFalse(service.startExecute(registerId, triggerCount));
    }
}

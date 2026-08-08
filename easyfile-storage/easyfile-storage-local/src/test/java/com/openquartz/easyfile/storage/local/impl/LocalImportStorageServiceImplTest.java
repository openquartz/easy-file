package com.openquartz.easyfile.storage.local.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.request.CancelImportRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.CancelImportResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.storage.local.entity.AsyncImportRecord;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportRecordMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportTaskMapper;
import org.junit.Before;
import org.junit.Test;

/**
 * LocalImportStorageServiceImpl unit tests with mocked mappers.
 */
public class LocalImportStorageServiceImplTest {

    private AsyncImportTaskMapper asyncImportTaskMapper;
    private AsyncImportRecordMapper asyncImportRecordMapper;
    private LocalImportStorageServiceImpl service;

    @Before
    public void setUp() {
        asyncImportTaskMapper = mock(AsyncImportTaskMapper.class);
        asyncImportRecordMapper = mock(AsyncImportRecordMapper.class);
        service = new LocalImportStorageServiceImpl(asyncImportTaskMapper, asyncImportRecordMapper);
    }

    @Test
    public void enableRunning_onlyFromNone() {
        Long registerId = 10L;

        AsyncImportRecord noneRecord = new AsyncImportRecord();
        noneRecord.setId(registerId);
        noneRecord.setImportStatus(ImportStatusEnum.NONE);
        noneRecord.setUpdateBy("system");

        when(asyncImportRecordMapper.findById(registerId)).thenReturn(noneRecord);
        when(asyncImportRecordMapper.refreshImportStatus(
            eq(registerId), eq(ImportStatusEnum.NONE), eq(ImportStatusEnum.EXECUTING), anyString()))
            .thenReturn(1);

        assertTrue(service.enableRunning(registerId));

        AsyncImportRecord executingRecord = new AsyncImportRecord();
        executingRecord.setId(registerId);
        executingRecord.setImportStatus(ImportStatusEnum.EXECUTING);
        executingRecord.setUpdateBy("system");

        clearInvocations(asyncImportRecordMapper);
        when(asyncImportRecordMapper.findById(registerId)).thenReturn(executingRecord);

        assertFalse(service.enableRunning(registerId));
        verify(asyncImportRecordMapper, never()).refreshImportStatus(
            eq(registerId), eq(ImportStatusEnum.NONE), eq(ImportStatusEnum.EXECUTING), anyString());
    }

    @Test
    public void enableRunning_returnsFalseWhenCasMisses() {
        Long registerId = 11L;
        AsyncImportRecord noneRecord = new AsyncImportRecord();
        noneRecord.setId(registerId);
        noneRecord.setImportStatus(ImportStatusEnum.NONE);
        noneRecord.setUpdateBy("system");

        when(asyncImportRecordMapper.findById(registerId)).thenReturn(noneRecord);
        when(asyncImportRecordMapper.refreshImportStatus(
            eq(registerId), eq(ImportStatusEnum.NONE), eq(ImportStatusEnum.EXECUTING), anyString()))
            .thenReturn(0);

        assertFalse(service.enableRunning(registerId));
    }

    @Test
    public void register_rejectsUnknownImportCode() {
        ImportRegisterRequest request = ImportRegisterRequest.builder()
            .appId("demo")
            .importCode("unknown-code")
            .ossUrl("http://oss/a.xlsx")
            .build();

        when(asyncImportTaskMapper.selectByImportCode("unknown-code", "demo")).thenReturn(null);

        try {
            service.register(request);
            fail("expected EasyFileException for missing import code");
        } catch (EasyFileException ex) {
            assertTrue(ex.getErrorCode() != null);
        }
        verify(asyncImportRecordMapper, never()).insertSelective(any());
    }

    @Test
    public void cancelImport_onlyFromNone() {
        Long registerId = 20L;
        CancelImportRequest request = new CancelImportRequest();
        request.setRegisterId(registerId);
        request.setCancelBy("alice");

        AsyncImportRecord noneRecord = new AsyncImportRecord();
        noneRecord.setId(registerId);
        noneRecord.setImportStatus(ImportStatusEnum.NONE);

        when(asyncImportRecordMapper.findById(registerId)).thenReturn(noneRecord);
        when(asyncImportRecordMapper.refreshImportStatus(
            eq(registerId), eq(ImportStatusEnum.NONE), eq(ImportStatusEnum.CANCEL), eq("alice")))
            .thenReturn(1);

        CancelImportResult ok = service.cancelImport(request);
        assertTrue(ok.isCancelResult());

        AsyncImportRecord executing = new AsyncImportRecord();
        executing.setId(registerId);
        executing.setImportStatus(ImportStatusEnum.EXECUTING);
        clearInvocations(asyncImportRecordMapper);
        when(asyncImportRecordMapper.findById(registerId)).thenReturn(executing);

        CancelImportResult rejected = service.cancelImport(request);
        assertFalse(rejected.isCancelResult());
        assertEquals("已经执行开始执行无法撤销", rejected.getCancelMsg());
        verify(asyncImportRecordMapper, never()).refreshImportStatus(
            eq(registerId), eq(ImportStatusEnum.NONE), eq(ImportStatusEnum.CANCEL), anyString());
    }

    @Test
    public void listImportResult_blankAppIdReturnsEmptyWithoutQuery() {
        ImportResultQueryRequest blankAppId = ImportResultQueryRequest.builder()
            .appId("")
            .build();
        blankAppId.setPageNum(2);
        blankAppId.setPageSize(10);

        Pagination<ImportResult> blankResult = service.listImportResult(blankAppId);
        assertNotNull(blankResult);
        assertEquals(Long.valueOf(0L), blankResult.getTotalRecords());
        assertEquals(Long.valueOf(10L), blankResult.getPageSize());
        assertTrue(blankResult.getModelList().isEmpty());
        verify(asyncImportRecordMapper, never()).countByCondition(any());
        verify(asyncImportRecordMapper, never()).selectByCondition(any());

        ImportResultQueryRequest nullAppId = ImportResultQueryRequest.builder()
            .appId(null)
            .build();
        clearInvocations(asyncImportRecordMapper);

        Pagination<ImportResult> nullResult = service.listImportResult(nullAppId);
        assertEquals(Long.valueOf(0L), nullResult.getTotalRecords());
        assertTrue(nullResult.getModelList().isEmpty());
        verify(asyncImportRecordMapper, never()).countByCondition(any());
        verify(asyncImportRecordMapper, never()).selectByCondition(any());
    }
}

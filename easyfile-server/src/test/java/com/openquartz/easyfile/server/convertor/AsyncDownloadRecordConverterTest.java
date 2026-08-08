package com.openquartz.easyfile.server.convertor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.common.response.DownloadResult;
import com.openquartz.easyfile.server.entity.AsyncDownloadRecord;
import com.openquartz.easyfile.server.entity.AsyncDownloadTask;
import java.util.Date;
import org.junit.Test;

/**
 * Null-safe conversion when task is missing.
 */
public class AsyncDownloadRecordConverterTest {

    @Test
    public void convertShouldTolerateNullTask() {
        AsyncDownloadRecord record = new AsyncDownloadRecord();
        record.setId(1L);
        record.setDownloadCode("code-a");
        record.setUploadStatus(UploadStatusEnum.SUCCESS);
        record.setFileSystem("local");
        record.setFileUrl("http://example.com/a.xlsx");
        record.setFileName("a.xlsx");
        record.setExecuteProcess(100);
        record.setCreateTime(new Date());
        record.setLastExecuteTime(new Date());
        record.setDownloadNum(0);
        record.setErrorMsg("");
        record.setUpdateBy("sys");

        DownloadResult result = AsyncDownloadRecordConverter.convert(record, null);
        assertNotNull(result);
        assertEquals("code-a", result.getDownloadCodeDesc());
    }

    @Test
    public void convertShouldUseTaskDescWhenPresent() {
        AsyncDownloadRecord record = new AsyncDownloadRecord();
        record.setId(2L);
        record.setDownloadCode("code-b");
        record.setUploadStatus(UploadStatusEnum.SUCCESS);
        record.setFileSystem("local");
        record.setFileUrl("http://example.com/b.xlsx");
        record.setFileName("b.xlsx");
        record.setExecuteProcess(100);
        record.setCreateTime(new Date());
        record.setLastExecuteTime(new Date());
        record.setDownloadNum(0);
        record.setErrorMsg("");
        record.setUpdateBy("sys");

        AsyncDownloadTask task = new AsyncDownloadTask();
        task.setTaskDesc("desc-b");

        DownloadResult result = AsyncDownloadRecordConverter.convert(record, task);
        assertEquals("desc-b", result.getDownloadCodeDesc());
    }
}

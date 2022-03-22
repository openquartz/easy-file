package org.svnee.easyfile.example.executor;

import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.annotation.FileExportExecutor;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;
import org.svnee.easyfile.starter.executor.BaseWrapperSyncResponseHeader;

@Component
@FileExportExecutor("ExampleExcelExecutor")
public class ExampleExcelExecutor implements BaseDownloadExecutor , BaseWrapperSyncResponseHeader {

    @Override
    public boolean enableAsync(DownloaderRequestContext context) {
        return true;
    }

    @Override
    public void export(DownloaderRequestContext context) {

    }
}

package org.svnee.easyfile.example.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.dictionary.FileSuffixEnum;
import org.svnee.easyfile.example.downloader.StudentStreamDownloadDemoExecutor;
import org.svnee.easyfile.example.entity.response.ExportResultVO;

/**
 * @author svnee
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {

    private final StudentStreamDownloadDemoExecutor studentStreamDownloadDemoExecutor;

    @PostMapping("/export")
    public ResponseResult<ExportResultVO> export(HttpServletResponse response) throws IOException {

        DownloaderRequestContext requestContext = new DownloaderRequestContext();
        requestContext.setOut(response.getOutputStream());
        requestContext.setFileSuffix(FileSuffixEnum.EXCEL_07.getFullFileSuffix());
        requestContext.setExportRemark("StudentExport备注");
        Pair<Boolean, Long> exportResult = studentStreamDownloadDemoExecutor.exportResult(requestContext);
        if (Boolean.TRUE.equals(exportResult.getKey())) {
            return ResponseResult.ok(new ExportResultVO(exportResult.getValue(), "导出成功"));
        }
        return null;
    }

}

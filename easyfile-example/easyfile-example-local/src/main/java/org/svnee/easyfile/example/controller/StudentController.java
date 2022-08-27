package org.svnee.easyfile.example.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.dictionary.FileSuffixEnum;
import org.svnee.easyfile.example.downloader.StudentDownloadDemoExecutor;
import org.svnee.easyfile.example.downloader.StudentMultiSheetPageDownloadDemoExecutor;
import org.svnee.easyfile.example.downloader.StudentMultiSheetStreamDownloadDemoExecutor;
import org.svnee.easyfile.example.downloader.StudentPageDownloadDemoExecutor;
import org.svnee.easyfile.example.downloader.StudentStreamDownloadDemoExecutor;
import org.svnee.easyfile.example.downloader.StudentStreamDownloadDemoMergeExecutor;
import org.svnee.easyfile.example.entity.response.ExportResultVO;

/**
 * @author svnee
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {

    private final StudentDownloadDemoExecutor studentDownloadDemoExecutor;
    private final StudentStreamDownloadDemoExecutor studentStreamDownloadDemoExecutor;
    private final StudentStreamDownloadDemoMergeExecutor studentStreamDownloadDemoMergeExecutor;
    private final StudentPageDownloadDemoExecutor studentPageDownloadDemoExecutor;
    private final StudentMultiSheetPageDownloadDemoExecutor studentMultiSheetPageDownloadDemoExecutor;
    private final StudentMultiSheetStreamDownloadDemoExecutor studentMultiSheetStreamDownloadDemoExecutor;


    @GetMapping("/export/get")
    public ResponseResult<ExportResultVO> getExport(HttpServletResponse response) throws IOException {
        DownloaderRequestContext requestContext = new DownloaderRequestContext();
        requestContext.setOut(response.getOutputStream());
        requestContext.setFileSuffix(FileSuffixEnum.EXCEL_07.getFullFileSuffix());
        requestContext.setExportRemark("StudentExport备注");
        Pair<Boolean, Long> result = studentDownloadDemoExecutor.exportResult(requestContext);
        if (Boolean.TRUE.equals(result.getKey())) {
            return ResponseResult.ok(new ExportResultVO(result.getValue(), "导出成功"));
        }
        return null;
    }

    @GetMapping("/export/stream")
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

    @GetMapping("/export/stream/merge")
    public ResponseResult<ExportResultVO> exportMerge(HttpServletResponse response) throws IOException {

        DownloaderRequestContext requestContext = new DownloaderRequestContext();
        requestContext.setOut(response.getOutputStream());
        requestContext.setFileSuffix(FileSuffixEnum.EXCEL_07.getFullFileSuffix());
        requestContext.setExportRemark("StudentExport备注");
        Pair<Boolean, Long> exportResult = studentStreamDownloadDemoMergeExecutor.exportResult(requestContext);
        if (Boolean.TRUE.equals(exportResult.getKey())) {
            return ResponseResult.ok(new ExportResultVO(exportResult.getValue(), "导出成功"));
        }
        return null;
    }

    @GetMapping("/export/page")
    public ResponseResult<ExportResultVO> exportPage(HttpServletResponse response) throws IOException {

        DownloaderRequestContext requestContext = new DownloaderRequestContext();
        requestContext.setOut(response.getOutputStream());
        requestContext.setFileSuffix(FileSuffixEnum.EXCEL_07.getFullFileSuffix());
        requestContext.setExportRemark("StudentExport备注");
        Pair<Boolean, Long> exportResult = studentPageDownloadDemoExecutor.exportResult(requestContext);
        if (Boolean.TRUE.equals(exportResult.getKey())) {
            return ResponseResult.ok(new ExportResultVO(exportResult.getValue(), "导出成功"));
        }
        return null;
    }

    @GetMapping("/export/multiSheet/page")
    public ResponseResult<ExportResultVO> exportPageMultiSheet(HttpServletResponse response) throws IOException {

        DownloaderRequestContext requestContext = new DownloaderRequestContext();
        requestContext.setOut(response.getOutputStream());
        requestContext.setFileSuffix(FileSuffixEnum.EXCEL_07.getFullFileSuffix());
        requestContext.setExportRemark("StudentExport备注");
        Pair<Boolean, Long> exportResult = studentMultiSheetPageDownloadDemoExecutor.exportResult(requestContext);
        if (Boolean.TRUE.equals(exportResult.getKey())) {
            return ResponseResult.ok(new ExportResultVO(exportResult.getValue(), "导出成功"));
        }
        return null;
    }

    @GetMapping("/export/multiSheet/stream")
    public ResponseResult<ExportResultVO> exportStreamMultiSheet(HttpServletResponse response) throws IOException {

        DownloaderRequestContext requestContext = new DownloaderRequestContext();
        requestContext.setOut(response.getOutputStream());
        requestContext.setFileSuffix(FileSuffixEnum.EXCEL_07.getFullFileSuffix());
        requestContext.setExportRemark("StudentExport备注");
        Pair<Boolean, Long> exportResult = studentMultiSheetStreamDownloadDemoExecutor.exportResult(requestContext);
        if (Boolean.TRUE.equals(exportResult.getKey())) {
            return ResponseResult.ok(new ExportResultVO(exportResult.getValue(), "导出成功"));
        }
        return null;
    }

}

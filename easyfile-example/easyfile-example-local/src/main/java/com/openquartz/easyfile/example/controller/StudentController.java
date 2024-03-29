package com.openquartz.easyfile.example.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openquartz.easyfile.common.bean.DownloaderRequestContext;
import com.openquartz.easyfile.common.bean.Notifier;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.dictionary.FileSuffixEnum;
import com.openquartz.easyfile.example.downloader.StudentDownloadDemoExecutor;
import com.openquartz.easyfile.example.downloader.StudentMultiSheetPageDownloadDemoExecutor;
import com.openquartz.easyfile.example.downloader.StudentMultiSheetStreamDownloadDemoExecutor;
import com.openquartz.easyfile.example.downloader.StudentPageDownloadDemoExecutor;
import com.openquartz.easyfile.example.downloader.StudentStreamDownloadDemoExecutor;
import com.openquartz.easyfile.example.downloader.StudentStreamDownloadDemoMergeExecutor;
import com.openquartz.easyfile.example.entity.response.ExportResultVO;

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
        Notifier notifier = new Notifier();
        notifier.setUserName("admin");
        notifier.setUserBy("admin");
        notifier.setEmail("admin@163.com");
        requestContext.setNotifier(notifier);
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
        Notifier notifier = new Notifier();
        notifier.setUserName("admin");
        notifier.setUserBy("admin");
        notifier.setEmail("admin@163.com");
        requestContext.setNotifier(notifier);
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
        Notifier notifier = new Notifier();
        notifier.setUserName("admin");
        notifier.setUserBy("admin");
        notifier.setEmail("admin@163.com");
        requestContext.setNotifier(notifier);
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
        requestContext.putParam("request", new TestRequest(1L));
        Notifier notifier = new Notifier();
        notifier.setUserName("admin");
        notifier.setUserBy("admin");
        notifier.setEmail("admin@163.com");
        requestContext.setNotifier(notifier);
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
        Notifier notifier = new Notifier();
        notifier.setUserName("admin");
        notifier.setUserBy("admin");
        notifier.setEmail("admin@163.com");
        requestContext.setNotifier(notifier);
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
        Notifier notifier = new Notifier();
        notifier.setUserName("admin");
        notifier.setUserBy("admin");
        notifier.setEmail("admin@163.com");
        requestContext.setNotifier(notifier);
        Pair<Boolean, Long> exportResult = studentMultiSheetStreamDownloadDemoExecutor.exportResult(requestContext);
        if (Boolean.TRUE.equals(exportResult.getKey())) {
            return ResponseResult.ok(new ExportResultVO(exportResult.getValue(), "导出成功"));
        }
        return null;
    }

}

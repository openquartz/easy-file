package com.openquartz.easyfile.example.controller;

import com.openquartz.easyfile.common.bean.ExportRequestContext;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openquartz.easyfile.common.bean.Notifier;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.dictionary.FileSuffixEnum;
import com.openquartz.easyfile.example.downloader.StudentExportDemoExecutor;
import com.openquartz.easyfile.example.downloader.StudentMultiSheetPageExportDemoExecutor;
import com.openquartz.easyfile.example.downloader.StudentMultiSheetStreamExportDemoExecutor;
import com.openquartz.easyfile.example.downloader.StudentPageExportDemoExecutor;
import com.openquartz.easyfile.example.downloader.StudentStreamExportDemoExecutor;
import com.openquartz.easyfile.example.downloader.StudentStreamExportDemoMergeExecutor;
import com.openquartz.easyfile.example.entity.response.ExportResultVO;

/**
 * @author svnee
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {

    private final StudentExportDemoExecutor studentDownloadDemoExecutor;
    private final StudentStreamExportDemoExecutor studentStreamDownloadDemoExecutor;
    private final StudentStreamExportDemoMergeExecutor studentStreamDownloadDemoMergeExecutor;
    private final StudentPageExportDemoExecutor studentPageDownloadDemoExecutor;
    private final StudentMultiSheetPageExportDemoExecutor studentMultiSheetPageDownloadDemoExecutor;
    private final StudentMultiSheetStreamExportDemoExecutor studentMultiSheetStreamDownloadDemoExecutor;


    @GetMapping("/export/get")
    public ResponseResult<ExportResultVO> getExport(HttpServletResponse response) throws IOException {
        ExportRequestContext requestContext = new ExportRequestContext();
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

        ExportRequestContext requestContext = new ExportRequestContext();
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

        ExportRequestContext requestContext = new ExportRequestContext();
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

        ExportRequestContext requestContext = new ExportRequestContext();
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

        ExportRequestContext requestContext = new ExportRequestContext();
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

        ExportRequestContext requestContext = new ExportRequestContext();
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

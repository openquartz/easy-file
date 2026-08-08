package com.openquartz.easyfile.example.controller;

import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.example.importer.StudentImportDemoExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 远程模式学生导入示例 API
 *
 * @author svnee
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentImportController {

    private final StudentImportDemoExecutor studentImportExecutor;

    @PostMapping("/import")
    public Pair<Boolean, Long> importStudents(@RequestParam String fileUrl,
                                              @RequestParam(required = false) String businessKey) {
        ImportRequestContext ctx = new ImportRequestContext();
        ctx.setSourceFileUrl(fileUrl);
        ctx.setFileType("xlsx");
        ctx.setBusinessUniqueKey(businessKey);
        return studentImportExecutor.importResult(ctx);
    }
}

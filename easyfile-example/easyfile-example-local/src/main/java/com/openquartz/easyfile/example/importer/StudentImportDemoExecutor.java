package com.openquartz.easyfile.example.importer;

import com.openquartz.easyfile.core.annotations.FileImportExecutor;
import com.openquartz.easyfile.core.context.ImportDataContext;
import com.openquartz.easyfile.core.context.ImportProcessContext;
import com.openquartz.easyfile.core.entity.ErrorRecord;
import com.openquartz.easyfile.core.entity.ProcessResult;
import com.openquartz.easyfile.core.entity.ValidationResult;
import com.openquartz.easyfile.core.executor.AbstractPagedImportExecutor;
import com.openquartz.easyfile.example.model.ImportStudentRow;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 学生导入示例执行器
 *
 * @author svnee
 */
@Slf4j
@Component
@FileImportExecutor(value = "studentImportDemo", desc = "Student导入示例", maxServerRetry = 1)
public class StudentImportDemoExecutor extends AbstractPagedImportExecutor<ImportStudentRow> {

    @Override
    protected Class<ImportStudentRow> getTargetClass() {
        return ImportStudentRow.class;
    }

    @Override
    public int getPageSize() {
        return 200;
    }

    @Override
    public ValidationResult validate(ImportDataContext<ImportStudentRow> dataContext) {
        List<ErrorRecord> errors = new ArrayList<>();
        List<ImportStudentRow> rows = dataContext.getData();
        long pageStart = (long) (dataContext.getPageNumber() - 1) * dataContext.getPageSize();
        for (int i = 0; i < rows.size(); i++) {
            ImportStudentRow row = rows.get(i);
            int rowNumber = (int) (pageStart + i + 1);
            if (row == null) {
                errors.add(ErrorRecord.of(rowNumber, "空行数据"));
                continue;
            }
            if (row.getName() == null || row.getName().trim().isEmpty()) {
                errors.add(ErrorRecord.of(rowNumber, "姓名不能为空"));
            }
            if (row.getAge() == null || row.getAge() < 0 || row.getAge() > 150) {
                errors.add(ErrorRecord.of(rowNumber, "年龄不合法"));
            }
        }
        if (errors.isEmpty()) {
            return ValidationResult.success();
        }
        return ValidationResult.failure(errors);
    }

    @Override
    public ProcessResult processBusiness(List<ImportStudentRow> data, ImportProcessContext processContext) {
        // 示例工程里只打印日志，真实项目中请替换为批量落库逻辑
        log.info(
            "[StudentImportDemoExecutor] page={}, startPos={}, importSize={}",
            processContext.getPageNumber(),
            processContext.getPageStartPosition(),
            data.size()
        );
        return ProcessResult.success(data.size());
    }
}

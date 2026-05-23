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
 * 远程模式学生导入示例执行器
 *
 * @author svnee
 */
@Slf4j
@Component
@FileImportExecutor(value = "studentImportRemoteDemo", desc = "Student远程导入示例")
public class StudentImportDemoExecutor extends AbstractPagedImportExecutor<ImportStudentRow> {

    @Override
    protected Class<ImportStudentRow> getTargetClass() {
        return ImportStudentRow.class;
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
            if (row.getSchool() == null || row.getSchool().trim().isEmpty()) {
                errors.add(ErrorRecord.of(rowNumber, "学校不能为空"));
            }
        }
        if (errors.isEmpty()) {
            return ValidationResult.success();
        }
        return ValidationResult.failure(errors);
    }

    @Override
    public ProcessResult processBusiness(List<ImportStudentRow> data, ImportProcessContext processContext) {
        // 远程样例里只展示处理入口，实际可调用业务服务或远程接口
        log.info(
            "[StudentImportDemoExecutor-remote] page={}, importSize={}",
            processContext.getPageNumber(),
            data.size()
        );
        return ProcessResult.success(data.size());
    }
}

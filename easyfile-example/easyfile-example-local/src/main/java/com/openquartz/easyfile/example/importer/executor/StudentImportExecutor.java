package com.openquartz.easyfile.example.importer.executor;

import com.openquartz.easyfile.common.bean.BaseImportRequestContext;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.core.annotations.FileImportExecutor;
import com.openquartz.easyfile.core.executor.BaseAsyncImportExecutor;
import com.openquartz.easyfile.example.importer.dto.StudentImportDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@FileImportExecutor(value = "student_import", desc = "Student Import")
public class StudentImportExecutor implements BaseAsyncImportExecutor<StudentImportDTO> {

    @Override
    public List<Pair<StudentImportDTO, String>> importData(List<StudentImportDTO> dataList, BaseImportRequestContext context) {
        List<Pair<StudentImportDTO, String>> failures = new ArrayList<>();
        
        for (StudentImportDTO student : dataList) {
            // Simulate processing
            log.info("Processing student: {}", student);
            if (student.getAge() != null && student.getAge() < 0) {
                failures.add(new Pair<>(student, "Age cannot be negative"));
            } else if (student.getName() == null || student.getName().isEmpty()) {
                failures.add(new Pair<>(student, "Name is required"));
            } else {
                // Save to DB (omitted for example)
                // In real scenario, you would insert into database here
            }
        }
        return failures;
    }

    @Override
    public Class<StudentImportDTO> getDataClass() {
        return StudentImportDTO.class;
    }

    @Override
    public int batchSize() {
        return 50;
    }
}

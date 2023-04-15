package com.openquartz.easyfile.example.downloader;

import com.openquartz.easyfile.example.model.School;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.util.page.Page;
import com.openquartz.easyfile.common.util.page.PageTotal;
import com.openquartz.easyfile.common.util.page.PageTotalContext;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.example.mapper.SchoolMapper;
import com.openquartz.easyfile.example.mapper.StudentMapper;
import com.openquartz.easyfile.example.model.Student;
import com.openquartz.easyfile.core.executor.impl.AbstractMultiSheetPageDownloadExcelExecutor;

/**
 * @author svnee
 **/
@Slf4j
@Component
@RequiredArgsConstructor
@FileExportExecutor("StudentMultiSheetPageDownloadDemoExecutor")
public class StudentMultiSheetPageDownloadDemoExecutor extends
    AbstractMultiSheetPageDownloadExcelExecutor<Student, School> {

    private final SchoolMapper schoolMapper;
    private final StudentMapper studentMapper;

    @Override
    public boolean enableAsync(BaseDownloaderRequestContext context) {
        return true;
    }

    @Override
    public List<School> sheetPrefix(BaseDownloaderRequestContext context) {
        return schoolMapper.selectAll().stream()
            .sorted((Comparator.comparing(School::getId)))
            .collect(Collectors.toList());
    }

    @Override
    public PageTotal count(Map<String, Object> othersMap, School sheetGroup) {
        int count = studentMapper.countBySchool(sheetGroup.getId());
        log.info("[>>>>>>>---count|School:{},name:{},total:{}]", sheetGroup.getId(), sheetGroup.getName(), count);
        PageTotalContext.cache(sheetGroup, PageTotal.of(count, 100));
        return PageTotalContext.currentPageToTal(sheetGroup);
    }

    @Override
    public Pair<Long, List<Student>> shardingData(BaseDownloaderRequestContext context, School sheetGroup, Page page,
        Long cursorId) {
        List<Student> studentList = studentMapper
            .findByMinIdAndSchoolIdLimit(cursorId, sheetGroup.getId(), page.getPageSize());
        if (CollectionUtils.isEmpty(studentList)) {
            return Pair.of(cursorId, studentList);
        }
        cursorId = studentList.get(studentList.size() - 1).getId();
        return Pair.of(cursorId, studentList);
    }
}

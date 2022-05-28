package org.svnee.easyfile.example.downloader;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.annotation.FileExportExecutor;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.bean.Page;
import org.svnee.easyfile.common.bean.PageTotal;
import org.svnee.easyfile.common.bean.PageTotalContext;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.example.mapper.SchoolMapper;
import org.svnee.easyfile.example.mapper.StudentMapper;
import org.svnee.easyfile.example.model.School;
import org.svnee.easyfile.example.model.Student;
import org.svnee.easyfile.starter.executor.impl.AbstractMultiSheetPageDownloadExcelExecutor;

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
    public boolean enableAsync(DownloaderRequestContext context) {
        return true;
    }

    @Override
    public List<School> sheetPrefix(DownloaderRequestContext context) {
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
    public Pair<Long, List<Student>> shardingData(DownloaderRequestContext context, School sheetGroup, Page page,
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

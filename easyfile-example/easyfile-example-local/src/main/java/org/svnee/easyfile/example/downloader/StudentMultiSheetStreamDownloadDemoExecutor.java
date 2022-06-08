package org.svnee.easyfile.example.downloader;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.annotations.FileExportExecutor;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.example.mapper.SchoolMapper;
import org.svnee.easyfile.example.mapper.StudentMapper;
import org.svnee.easyfile.example.model.School;
import org.svnee.easyfile.example.model.Student;
import org.svnee.easyfile.starter.executor.impl.AbstractMultiSheetStreamDownloadExcelExecutor;

/**
 * @author svnee
 **/
@Slf4j
@Component
@RequiredArgsConstructor
@FileExportExecutor("StudentMultiSheetStreamDownloadDemoExecutor")
public class StudentMultiSheetStreamDownloadDemoExecutor extends
    AbstractMultiSheetStreamDownloadExcelExecutor<SqlSession, Cursor<Student>, Student, School> {

    private final SqlSessionFactory sqlSessionFactory;
    private final SchoolMapper schoolMapper;

    @Override
    public Integer enhanceLength() {
        return 100;
    }

    @Override
    public boolean enableAsync(DownloaderRequestContext context) {
        return true;
    }

    @Override
    public SqlSession openSession() {
        return sqlSessionFactory.openSession();
    }

    @Override
    public List<School> sheetPrefix() {
        return schoolMapper.selectAll().stream()
            .sorted((Comparator.comparing(School::getId)))
            .collect(Collectors.toList());
    }

    @Override
    public Cursor<Student> streamQuery(SqlSession session, DownloaderRequestContext context, School sheetGroup) {
        return session.getMapper(StudentMapper.class).scanBySchool(10000000, sheetGroup.getId());
    }
}

package com.openquartz.easyfile.example.downloader;

import com.openquartz.easyfile.common.bean.BaseExportRequestContext;
import com.openquartz.easyfile.example.model.School;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.example.mapper.SchoolMapper;
import com.openquartz.easyfile.example.mapper.StudentMapper;
import com.openquartz.easyfile.example.model.Student;
import com.openquartz.easyfile.core.executor.impl.AbstractStreamExportExcelExecutor;

/**
 * @author svnee
 **/
@Component
@FileExportExecutor(value = "studentStreamDownloadDemo", desc = "Student导出", cacheKey = {"age"})
public class StudentStreamExportDemoExecutor extends
    AbstractStreamExportExcelExecutor<SqlSession, Cursor<Student>, Student> {

    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private SchoolMapper schoolMapper;

    @Override
    public Integer enhanceLength() {
        return 100;
    }

    @Override
    public List<Student> enhance(List<Student> students) {
        List<Long> schoolIdList = students.stream()
            .map(Student::getSchoolId)
            .distinct()
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(schoolIdList)) {
            return students;
        }
        List<School> schoolList = schoolMapper.selectByIdList(schoolIdList);
        Map<Long, String> schoolMap = schoolList.stream()
            .collect(Collectors.toMap(School::getId, School::getGrade));
        for (Student student : students) {
            student.setSchoolGrade(schoolMap.get(student.getSchoolId()));
        }
        return students;
    }

    @Override
    public boolean enableAsync(BaseExportRequestContext context) {
        return true;
    }

    @Override
    public SqlSession openSession() {
        return sqlSessionFactory.openSession();
    }

    @Override
    public Cursor<Student> streamQuery(SqlSession session, BaseExportRequestContext context) {
        return session.getMapper(StudentMapper.class).scan(10000000);
    }
}

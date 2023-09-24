package com.openquartz.easyfile.example.downloader;

import com.google.common.collect.ImmutableList;
import com.openquartz.easyfile.common.bean.BaseExporterRequestContext;
import com.openquartz.easyfile.core.executor.impl.AbstractStreamExportExcelExecutor;
import com.openquartz.easyfile.example.model.Address;
import com.openquartz.easyfile.example.model.Feature;
import com.openquartz.easyfile.example.model.School;
import java.util.Date;
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

/**
 * @author svnee
 **/
@Component
@FileExportExecutor(value = "StudentStreamDownloadDemoMergeExecutor", desc = "Student导出", cacheKey = {"age"})
public class StudentStreamExportDemoMergeExecutor extends
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

        Feature feature1 = new Feature();
        feature1.setId(1);
        feature1.setName("特征1");
        feature1.setDesc("特征1描述");
        feature1.setCode("T1");

        Feature feature2 = new Feature();
        feature2.setId(2);
        feature2.setName("特征2");
        feature2.setDesc("特征2描述");
        feature2.setCode("T2");

        for (Student student : students) {
            student.setSchoolGrade(schoolMap.get(student.getSchoolId()));
            student.setFeatureList(ImmutableList.of(feature1, feature2));
            student.setAddr(new Address("地址1", new Date()));
        }
        return students;
    }

    @Override
    public boolean enableAsync(BaseExporterRequestContext context) {
        return true;
    }

    @Override
    public SqlSession openSession() {
        return sqlSessionFactory.openSession();
    }

    @Override
    public Cursor<Student> streamQuery(SqlSession session, BaseExporterRequestContext context) {
        return session.getMapper(StudentMapper.class).scan(1000);
    }
}

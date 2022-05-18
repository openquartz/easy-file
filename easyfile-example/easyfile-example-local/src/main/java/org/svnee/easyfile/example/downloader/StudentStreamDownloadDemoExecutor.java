package org.svnee.easyfile.example.downloader;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.annotation.FileExportExecutor;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.example.mapper.StudentMapper;
import org.svnee.easyfile.example.model.Address;
import org.svnee.easyfile.example.model.Feature;
import org.svnee.easyfile.example.model.Student;
import org.svnee.easyfile.starter.executor.impl.AbstractStreamDownloadExcelExecutor;

/**
 * @author svnee
 **/
@Component
@FileExportExecutor(value = "studentStreamDownloadDemo", desc = "Student导出", cacheKey = {"age"})
public class StudentStreamDownloadDemoExecutor extends
    AbstractStreamDownloadExcelExecutor<SqlSession, Cursor<Student>, Student> {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public boolean enableAsync(DownloaderRequestContext context) {
        return false;
    }

    @Override
    public SqlSession openSession() {
        return sqlSessionFactory.openSession();
    }

    @Override
    public Cursor<Student> streamQuery(SqlSession session, DownloaderRequestContext context) {
        return session.getMapper(StudentMapper.class).scan(10000);
    }

    @Override
    public List<Student> enhance(List<Student> students) {
        Feature feature = new Feature();
        feature.setId(1);
        feature.setName("1号特征");
        feature.setDesc("特征A");
        feature.setCode("A");

        Feature feature1 = new Feature();
        feature1.setId(2);
        feature1.setName("2号特征");
        feature1.setDesc("特征B");
        feature1.setCode("B");

        Address address = new Address();
        address.setAddress("上海古北路");
        address.setExpireTime(new Date());

        for (Student student : students) {
            student.setFeatureList(Lists.newArrayList(feature, feature1));
            student.setAddr(address);
        }

        return students;
    }
}

package org.svnee.easyfile.example.downloader;

import javax.annotation.Resource;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;
import org.svnee.easyfile.core.annotations.FileExportExecutor;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.example.mapper.StudentMapper;
import org.svnee.easyfile.example.model.Student;
import org.svnee.easyfile.core.executor.impl.AbstractStreamDownloadExcelExecutor;

/**
 * @author svnee
 **/
@Component
@FileExportExecutor(value = "studentStreamDownloadDemo", desc = "Student导出")
public class StudentStreamDownloadDemoExecutor extends
    AbstractStreamDownloadExcelExecutor<SqlSession, Cursor<Student>, Student> {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public boolean enableAsync(BaseDownloaderRequestContext context) {
        return false;
    }

    @Override
    public SqlSession openSession() {
        return sqlSessionFactory.openSession();
    }

    @Override
    public Cursor<Student> streamQuery(SqlSession session, BaseDownloaderRequestContext context) {
        return session.getMapper(StudentMapper.class).scan(1000000);
    }
}

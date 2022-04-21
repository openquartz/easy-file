package org.svnee.easyfile.example.downloader;

import javax.annotation.Resource;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.annotation.FileExportExecutor;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.example.mapper.StudentMapper;
import org.svnee.easyfile.example.model.Student;
import org.svnee.easyfile.starter.executor.impl.AbstractStreamDownloadExcelExecutor;

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
    public boolean enableAsync(DownloaderRequestContext context) {
        return false;
    }

    @Override
    public SqlSession openSession() {
        return sqlSessionFactory.openSession();
    }

    @Override
    public Cursor<Student> streamQuery(SqlSession session, DownloaderRequestContext context) {
        return session.getMapper(StudentMapper.class).scan(1000000);
    }
}

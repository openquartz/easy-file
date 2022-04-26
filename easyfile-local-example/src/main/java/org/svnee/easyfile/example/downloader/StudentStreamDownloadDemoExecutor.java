package org.svnee.easyfile.example.downloader;

import java.lang.reflect.Method;
import javax.annotation.Resource;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.annotation.FileExportExecutor;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.example.mapper.StudentMapper;
import org.svnee.easyfile.example.model.Student;
import org.svnee.easyfile.starter.executor.impl.AbstractStreamDownloadExcelExecutor;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * @author svnee
 * @desc
 **/
@Component
@FileExportExecutor(value = "studentStreamDownloadDemo", desc = "Student导出")
public class StudentStreamDownloadDemoExecutor extends
    AbstractStreamDownloadExcelExecutor<SqlSession, Cursor<Student>, Student> {

    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UploadService uploadService;

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
        return session.getMapper(StudentMapper.class).scan(100);
    }
}

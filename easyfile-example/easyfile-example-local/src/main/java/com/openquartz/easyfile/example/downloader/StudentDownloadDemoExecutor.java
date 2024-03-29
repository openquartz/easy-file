package com.openquartz.easyfile.example.downloader;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.bean.DownloaderRequestContext;
import com.openquartz.easyfile.example.mapper.StudentMapper;
import com.openquartz.easyfile.example.model.Student;
import com.openquartz.easyfile.core.executor.impl.AbstractDownloadExcel07Executor;

/**
 * 普通异步下载
 *
 * @author svnee
 **/
@Component
@FileExportExecutor("StudentDownloadDemoExecutor")
public class StudentDownloadDemoExecutor extends AbstractDownloadExcel07Executor {

    @Resource
    private StudentMapper studentMapper;

    @Override
    public boolean enableAsync(BaseDownloaderRequestContext context) {
        return true;
    }

    @Override
    public void export(DownloaderRequestContext context) {
        List<Student> list = studentMapper.selectAll();
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(context.getOut(), Student.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            excelWriter.write(list, writeSheet);
        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }
}

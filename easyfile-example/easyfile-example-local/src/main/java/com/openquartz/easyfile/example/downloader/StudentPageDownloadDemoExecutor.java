package com.openquartz.easyfile.example.downloader;

import com.openquartz.easyfile.example.excel.WaterMarkExcelIntensifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.util.page.Page;
import com.openquartz.easyfile.common.util.page.PageTotal;
import com.openquartz.easyfile.common.util.page.PageTotalContext;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.example.mapper.StudentMapper;
import com.openquartz.easyfile.example.model.Student;
import com.openquartz.easyfile.core.executor.excel.ExcelIntensifier;
import com.openquartz.easyfile.core.executor.impl.AbstractPageDownloadExcelExecutor;

/**
 * @author svnee
 **/
@Component
@FileExportExecutor(value = "StudentPageDownloadDemoExecutor", desc = "Student分页下载")
public class StudentPageDownloadDemoExecutor extends AbstractPageDownloadExcelExecutor<Student> {

    @Resource
    private StudentMapper studentMapper;

    @Override
    public boolean enableAsync(BaseDownloaderRequestContext context) {
        return true;
    }

    @Override
    public String sheetPrefix() {
        return "学生信息";
    }

    @Override
    public PageTotal count(Map<String, Object> othersMap) {
        if (PageTotalContext.currentPageToTal(sheetPrefix()) != null) {
            return PageTotalContext.currentPageToTal(sheetPrefix());
        }
        int count = studentMapper.count();
        PageTotalContext.cache(sheetPrefix(), PageTotal.of(count, 100));
        return PageTotalContext.currentPageToTal(sheetPrefix());
    }

    @Override
    public Pair<Long, List<Student>> shardingData(BaseDownloaderRequestContext context, Page page, Long cursorId) {
        List<Student> studentList = studentMapper.findByMinIdLimit(cursorId, page.getPageSize());
        if (CollectionUtils.isEmpty(studentList)) {
            return Pair.of(cursorId, studentList);
        }
        cursorId = studentList.get(studentList.size() - 1).getId();
        return Pair.of(cursorId, studentList);
    }

    /**
     * 水印打印
     */
    @Override
    public List<ExcelIntensifier> enhanceExcel() {
        return Collections.singletonList(new WaterMarkExcelIntensifier());
    }
}

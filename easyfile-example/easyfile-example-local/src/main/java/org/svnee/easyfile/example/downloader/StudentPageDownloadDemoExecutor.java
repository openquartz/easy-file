package org.svnee.easyfile.example.downloader;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.annotation.FileExportExecutor;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.bean.Page;
import org.svnee.easyfile.common.bean.PageTotal;
import org.svnee.easyfile.common.bean.PageTotalContext;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.example.mapper.StudentMapper;
import org.svnee.easyfile.example.model.Student;
import org.svnee.easyfile.starter.executor.impl.AbstractPageDownloadExcelExecutor;

/**
 * @author svnee
 **/
@Component
@FileExportExecutor(value = "StudentPageDownloadDemoExecutor")
public class StudentPageDownloadDemoExecutor extends AbstractPageDownloadExcelExecutor<Student> {

    @Resource
    private StudentMapper studentMapper;

    @Override
    public boolean enableAsync(DownloaderRequestContext context) {
        return true;
    }

    @Override
    public boolean enableExportCache(BaseDownloaderRequestContext context) {
        return true;
    }

    @Override
    public PageTotal count(Map<String, Object> othersMap) {
        if (PageTotalContext.currentPageToTal() != null) {
            return PageTotalContext.currentPageToTal();
        }
        int count = studentMapper.count();
        PageTotalContext.cache(PageTotal.of(count, 100));
        return PageTotalContext.currentPageToTal();
    }

    @Override
    public Pair<Long, List<Student>> shardingData(DownloaderRequestContext context, Page page, Long maxId) {
        List<Student> studentList = studentMapper.findByMinIdLimit(maxId, page.getPageNum());
        if (CollectionUtils.isEmpty(studentList)) {
            return Pair.of(maxId, studentList);
        }
        maxId = studentList.get(studentList.size() - 1).getId();
        return Pair.of(maxId, studentList);
    }
}

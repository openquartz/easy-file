package org.svnee.easyfile.example.downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.annotations.FileExportExecutor;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.bean.Page;
import org.svnee.easyfile.common.bean.PageTotal;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.example.model.ExcelModel;
import org.svnee.easyfile.starter.executor.impl.AbstractPageDownloadExcelExecutor;

/**
 * Excel download demo
 *
 * @author svnee
 */
@Component
@FileExportExecutor(value = "excelDownloaderDemo", maxServerRetry = 1, desc = "Excel导出", cacheKey = {"#request.id"})
public class ExcelDownloadDemoExecutor extends AbstractPageDownloadExcelExecutor<ExcelModel> {

    @Override
    public PageTotal count(Map<String, Object> othersMap) {
        return PageTotal.of(0, 3);
    }

    @Override
    public Pair<Long, List<ExcelModel>> shardingData(BaseDownloaderRequestContext context, Page page, Long maxId) {

        ArrayList<ExcelModel> list = Lists.newArrayList();
        for (int i = 0; i < page.getTotal(); i++) {
            list.add(new ExcelModel("title_" + i, "author_" + i));
        }
        List<ExcelModel> subList = list
            .subList((page.getPageNum() - 1) * page.getPageSize(), page.getPageNum() * page.getPageSize());
        return Pair.of(0L, subList);
    }

    @Override
    public boolean enableAsync(BaseDownloaderRequestContext context) {
        return false;
    }
}

package com.openquartz.easyfile.example.downloader;

import com.openquartz.easyfile.example.model.ExcelModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.util.page.Page;
import com.openquartz.easyfile.common.util.page.PageTotal;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.core.executor.impl.AbstractPageDownloadExcelExecutor;

/**
 * Excel download demo
 *
 * @author svnee
 */
@Component
@FileExportExecutor(value = "excelDownloaderDemo", maxServerRetry = 1, desc = "Excel导出")
public class ExcelDownloadDemoExecutor extends AbstractPageDownloadExcelExecutor<ExcelModel> {

    @Override
    public PageTotal count(Map<String, Object> othersMap) {
        return PageTotal.of(0, 3);
    }

    @Override
    public Pair<Long, List<ExcelModel>> shardingData(BaseDownloaderRequestContext context, Page page, Long cursorId) {

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

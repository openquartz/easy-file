package org.svnee.easyfile.starter.executor.impl;

import java.util.List;
import java.util.Objects;
import javax.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.bean.Page;
import org.svnee.easyfile.common.bean.PageTotal;
import org.svnee.easyfile.common.bean.PageTotalContext;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.common.bean.excel.ExcelBean;
import org.svnee.easyfile.common.bean.excel.ExcelBeanUtils;
import org.svnee.easyfile.common.bean.excel.ExcelExports;
import org.svnee.easyfile.common.bean.excel.ExcelFiled;
import org.svnee.easyfile.common.util.GenericUtils;
import org.svnee.easyfile.common.util.PageUtil;
import org.svnee.easyfile.starter.executor.PageShardingDownloadExecutor;
import org.svnee.easyfile.starter.executor.excel.ExcelIntensifierExecutor;

/**
 * 分页下载Excel执行器
 * 主要用于大 Excel 文件的导出。目前仅支持07版excel
 * 同时 <T> 泛型为导出实体类对象
 * 对象导出需要配合注解使用{@link org.svnee.easyfile.common.annotations.ExcelProperty}
 *
 * @author svnee
 */
@Slf4j
public abstract class AbstractPageDownloadExcelExecutor<T>
    extends AbstractDownloadExcel07Executor
    implements PageShardingDownloadExecutor<T>, ExcelIntensifierExecutor {

    /**
     * 导出模板类分组 {@link org.svnee.easyfile.common.annotations.ExcelProperty#group()}
     *
     * @param context context
     * @return Class<?>
     */
    public Class<?>[] exportGroup(BaseDownloaderRequestContext context) {
        return new Class<?>[]{Default.class};
    }

    /**
     * SheetName Prefix
     *
     * @return sheetPrefix
     */
    public String sheetPrefix() {
        return ExcelBean.DEFAULT_SHEET_GROUP;
    }

    /**
     * 导出实现
     */
    @Override
    public void export(DownloaderRequestContext context) {
        PageTotal total;
        if (PageTotalContext.currentPageToTal(sheetPrefix()) != null) {
            total = PageTotalContext.currentPageToTal(sheetPrefix());
        } else {
            // 执行 PageTotal 查询
            total = count(context.getOtherMap());
        }
        try (ExcelBean excelBean = ExcelExports.createWorkbook()) {
            List<ExcelFiled> fieldList = ExcelBeanUtils
                .getExcelFiledByGroup(GenericUtils.getClassT(this, 0), exportGroup(context));

            if (total.getTotal() <= 0) {
                // 无结果导出
                ExcelExports.writeHeader(excelBean, fieldList, sheetPrefix());
                ExcelExports.writeWorkbook(excelBean, context.getOut());
                return;
            }
            int totalPage = PageUtil.totalPage(total.getTotal(), total.getPageSize());
            Page page = new Page(1, total.getPageSize(), total.getTotal());
            Long cursorId = 0L;
            for (int i = 0; i < totalPage; i++) {
                page.setPageNum(i + 1);
                Pair<Long, List<T>> pair = shardingData(context, page, cursorId);
                if (Objects.nonNull(pair)) {
                    cursorId = pair.getKey();
                    ExcelExports.writeData(excelBean, fieldList, pair.getValue(), sheetPrefix());
                }
            }
            excelBean.logExportInfo(log);
            this.executeEnhance(excelBean.getWorkbook(), context);
            ExcelExports.writeWorkbook(excelBean, context.getOut());
        }
    }

}

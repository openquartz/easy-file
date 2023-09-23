package com.openquartz.easyfile.core.executor.impl;

import com.openquartz.easyfile.core.executor.ExecuteProcessProbe;
import com.openquartz.easyfile.core.executor.excel.ExcelIntensifierExecutor;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.bean.DownloaderRequestContext;
import com.openquartz.easyfile.common.util.page.Page;
import com.openquartz.easyfile.common.util.page.PageTotal;
import com.openquartz.easyfile.common.util.page.PageTotalContext;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.bean.excel.ExcelBean;
import com.openquartz.easyfile.common.bean.excel.ExcelBeanUtils;
import com.openquartz.easyfile.common.bean.excel.ExcelExports;
import com.openquartz.easyfile.common.bean.excel.ExcelFiled;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.GenericUtils;
import com.openquartz.easyfile.common.util.PageUtil;
import com.openquartz.easyfile.common.annotations.ExcelProperty;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;

/**
 * 多sheet分割导出
 *
 * @author svnee
 **/
@Slf4j
public abstract class AbstractMultiSheetPageDownloadExcelExecutor<T, G>
    extends AbstractDownloadExcel07Executor
    implements BaseDownloadExecutor, ExcelIntensifierExecutor {

    /**
     * 导出模板类分组 {@link ExcelProperty#group()}
     *
     * @param context context
     * @return export class group
     */
    public Class<?>[] exportGroup(BaseDownloaderRequestContext context) {
        return new Class<?>[]{Default.class};
    }

    /**
     * 所有的需要分页的sheet的导出
     *
     * @param context context
     * @return sheet-group
     */
    public abstract List<G> sheetPrefix(BaseDownloaderRequestContext context);

    /**
     * 用于传递参数
     *
     * @param othersMap 参数map
     * @param sheetGroup sheetGroup
     * @return 计算分页信息
     */
    public abstract PageTotal count(Map<String, Object> othersMap, G sheetGroup);

    /**
     * 分页导出
     *
     * @param context 上下文请求
     * @param sheetGroup sheetGroup
     * @param page 页
     * @param cursorId 当前滚动的分页的游标ID,可以是使用ID 做最大最小值传递。主要是用于优化传递分页查询速度
     * @return 当前最大ID key: cursorId, value: resultList
     */
    public abstract Pair<Long, List<T>> shardingData(BaseDownloaderRequestContext context, G sheetGroup, Page page,
        Long cursorId);

    @Override
    public void export(DownloaderRequestContext context) {

        List<G> sheetGroupList = sheetPrefix(context);

        if (CollectionUtils.isEmpty(sheetGroupList)) {
            ExcelExports.writeEmptyWorkbook(context.getOut());
            return;
        }

        try (ExcelBean excelBean = ExcelExports.createWorkbook()) {
            List<ExcelFiled> fieldList = ExcelBeanUtils
                .getExcelFiledByGroup(GenericUtils.getClassT(this, 0), exportGroup(context));

            for (int j = 0; j < sheetGroupList.size(); j++) {
                PageTotal total;
                G sheetGroup = sheetGroupList.get(j);
                if (PageTotalContext.currentPageToTal(sheetGroup) != null) {
                    total = PageTotalContext.currentPageToTal(sheetGroup);
                } else {
                    // 执行 PageTotal 查询
                    total = count(context.getOtherMap(), sheetGroup);
                }
                if (total.getTotal() <= 0) {
                    // 无结果导出
                    ExcelExports.writeHeader(excelBean, fieldList, sheetGroup.toString());
                    continue;
                }
                int totalPage = PageUtil.totalPage(total.getTotal(), total.getPageSize());
                Page page = new Page(1, total.getPageSize(), total.getTotal());
                Long cursorId = 0L;
                for (int i = 0; i < totalPage; i++) {
                    page.setPageNum(i + 1);
                    Pair<Long, List<T>> pair = shardingData(context, sheetGroup, page, cursorId);
                    if (Objects.nonNull(pair)) {
                        cursorId = pair.getKey();
                        ExcelExports.writeData(excelBean, fieldList, pair.getValue(), sheetGroup.toString());
                    }
                    // 执行进度
                    int executeProcess = (int) (((i + 1) / (totalPage * Constants.DOUBLE_ONE))
                        * ((j + 1) / (sheetGroupList.size() * Constants.DOUBLE_ONE)) * Constants.FULL_PROCESS);
                    ExecuteProcessProbe.report(executeProcess);
                }
            }

            excelBean.logExportInfo(log);
            // 增强Excel
            executeEnhance(excelBean.getWorkbook(), context);
            ExcelExports.writeWorkbook(excelBean, context.getOut());
        }
    }

}

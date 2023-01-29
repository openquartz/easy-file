package org.svnee.easyfile.starter.executor.impl;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.bean.excel.ExcelBean;
import org.svnee.easyfile.common.bean.excel.ExcelBeanUtils;
import org.svnee.easyfile.common.bean.excel.ExcelExports;
import org.svnee.easyfile.common.bean.excel.ExcelFiled;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.GenericUtils;
import org.svnee.easyfile.starter.executor.StreamDownloadExecutor;
import org.svnee.easyfile.starter.executor.excel.ExcelIntensifierExecutor;
import org.svnee.easyfile.starter.executor.ExecuteProcessProbe;

/**
 * 多Sheet流式导出
 *
 * @author snvee
 **/
@Slf4j
public abstract class AbstractMultiSheetStreamDownloadExcelExecutor<S extends Closeable, R extends Iterable<T>, T, G>
    extends AbstractDownloadExcel07Executor
    implements StreamDownloadExecutor<S>, ExcelIntensifierExecutor {

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
    public abstract List<G> sheetPrefix();

    /**
     * 增强类的字段
     *
     * @param tList t
     */
    public List<T> enhance(List<T> tList, G sheetGroup) {
        return tList;
    }

    /**
     * 查询结果
     *
     * @param context context
     * @param session session会话
     * @param sheetGroup sheetGroup
     * @return 流式查询结果
     */
    public abstract R streamQuery(S session, BaseDownloaderRequestContext context, G sheetGroup);

    @Override
    public void export(DownloaderRequestContext context) {

        List<G> sheetGroupList = sheetPrefix();
        if (CollectionUtils.isEmpty(sheetGroupList)) {
            ExcelExports.writeEmptyWorkbook(context.getOut());
            return;
        }

        // 创建workbook
        try (ExcelBean excelBean = ExcelExports.createWorkbook()) {
            List<ExcelFiled> fieldList = ExcelBeanUtils
                .getExcelFiledByGroup(GenericUtils.getClassT(this, 2), exportGroup(context));

            // 调用流式查询
            S session = openSession();
            R iterable = null;
            try {
                for (int j = 0; j < sheetGroupList.size(); j++) {
                    G sheetGroup = sheetGroupList.get(j);
                    // 设置表头header
                    ExcelExports.writeHeader(excelBean, fieldList, sheetGroup.toString());

                    iterable = streamQuery(session, context, sheetGroup);
                    // 流式的进行数据导出,并对相关字段做增强操作
                    List<T> tempList = new ArrayList<>();
                    iterable
                        .forEach(t -> {
                            if (tempList.size() >= enhanceLength()) {
                                // 写入数据
                                ExcelExports.writeData(excelBean, fieldList, enhance(tempList, sheetGroup),
                                    sheetGroup.toString());
                                // 清除临时数据
                                tempList.clear();
                            }
                            tempList.add(t);
                        });
                    if (!tempList.isEmpty()) {
                        ExcelExports
                            .writeData(excelBean, fieldList, enhance(tempList, sheetGroup), sheetGroup.toString());
                        // 清除临时数据
                        tempList.clear();
                    }
                    // 上报进度
                    ExecuteProcessProbe.report((j + 1) / sheetGroupList.size() * Constants.FULL_PROCESS);
                }
                excelBean.logExportInfo(log);
                // 增强
                this.executeEnhance(excelBean.getWorkbook(), context);
                ExcelExports.writeWorkbook(excelBean, context.getOut());
            } finally {
                if (Objects.nonNull(session)) {
                    try {
                        session.close();
                    } catch (Exception ignored) {
                    }
                }
                if (Objects.nonNull(iterable) && iterable instanceof Closeable) {
                    try {
                        ((Closeable) iterable).close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}

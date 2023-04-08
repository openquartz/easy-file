package com.openquartz.easyfile.core.executor.impl;

import com.openquartz.easyfile.core.executor.excel.ExcelIntensifierExecutor;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.bean.DownloaderRequestContext;
import com.openquartz.easyfile.common.bean.excel.ExcelBean;
import com.openquartz.easyfile.common.bean.excel.ExcelBeanUtils;
import com.openquartz.easyfile.common.bean.excel.ExcelExports;
import com.openquartz.easyfile.common.bean.excel.ExcelFiled;
import com.openquartz.easyfile.common.util.GenericUtils;
import com.openquartz.easyfile.common.annotations.ExcelProperty;
import com.openquartz.easyfile.core.executor.StreamDownloadExecutor;

/**
 * 流式下载导出-支持Mybatis 流式查询。查询出结果数据，进行不停的将数据导出到磁盘上
 * 用户使用导出文件时,必须对模版类使用{@link ExcelProperty} 注解,用来标识excel需要导出的标题字段
 *
 * @param <S> Session - 流式查询会话对象
 * @param <R> 流式查询的结果集合-需要支持为迭代器
 * @param <T> 导出实体模版类对象
 * @author svnee
 **/
@Slf4j
public abstract class AbstractStreamDownloadExcelExecutor<S extends Closeable, R extends Iterable<T>, T>
    extends AbstractDownloadExcel07Executor
    implements StreamDownloadExecutor<S>, ExcelIntensifierExecutor {

    /**
     * 导出模板类分组 {@link ExcelProperty#group()}
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
     * 增强类的字段
     *
     * @param tList t
     */
    public List<T> enhance(List<T> tList) {
        return tList;
    }

    /**
     * 查询结果
     *
     * @param context context
     * @param session session会话
     * @return 流式查询结果
     */
    public abstract R streamQuery(S session, BaseDownloaderRequestContext context);

    @Override
    public void export(DownloaderRequestContext context) {
        // 创建workbook
        try (ExcelBean excelBean = ExcelExports.createWorkbook()) {
            List<ExcelFiled> fieldList = ExcelBeanUtils
                .getExcelFiledByGroup(GenericUtils.getClassT(this, 2), exportGroup(context));
            // 设置表头header
            ExcelExports.writeHeader(excelBean, fieldList, sheetPrefix());

            // 调用流式查询
            S session = null;
            R iterable = null;
            try {
                session = openSession();
                iterable = streamQuery(session, context);
                // 流式的进行数据导出,并对相关字段做增强操作
                List<T> tempList = new ArrayList<>();
                iterable
                    .forEach(t -> {
                        if (tempList.size() >= enhanceLength()) {
                            // 写入数据
                            ExcelExports
                                .writeData(excelBean, fieldList, enhance(tempList), sheetPrefix());
                            // 清除临时数据
                            tempList.clear();
                        }
                        tempList.add(t);
                    });
                if (!tempList.isEmpty()) {
                    ExcelExports.writeData(excelBean, fieldList, enhance(tempList), sheetPrefix());
                    // 清除临时数据
                    tempList.clear();
                }
                excelBean.logExportInfo(log);
                // 增强Excel
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

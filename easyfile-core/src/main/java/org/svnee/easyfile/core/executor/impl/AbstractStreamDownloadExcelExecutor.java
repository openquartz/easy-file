package org.svnee.easyfile.core.executor.impl;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.groups.Default;
import org.svnee.easyfile.common.annotation.ExcelProperty;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.bean.excel.ExcelBean;
import org.svnee.easyfile.common.bean.excel.ExcelBeanUtils;
import org.svnee.easyfile.common.bean.excel.ExcelExports;
import org.svnee.easyfile.common.util.GenericUtils;
import org.svnee.easyfile.core.executor.StreamDownloadExecutor;

/**
 * 流式下载导出-支持Mybatis 流式查询。查询出结果数据，进行不停的将数据导出到磁盘上
 * 用户使用导出文件时,必须对模版类使用{@link org.svnee.easyfile.common.annotation.ExcelProperty} 注解,用来标识excel需要导出的标题字段
 *
 * @param <S> Session - 流式查询会话对象
 * @param <R> 流式查询的结果集合-需要支持为迭代器
 * @param <T> 导出实体模版类对象
 * @author svnee
 **/
public abstract class AbstractStreamDownloadExcelExecutor<S extends Closeable, R extends Iterable<T>, T>
    implements StreamDownloadExecutor<S, R, T> {

    /**
     * 导出模板类分组 {@link ExcelProperty#group()}
     *
     * @param context context
     * @return Class<?>
     */
    public Class<?>[] exportGroup(DownloaderRequestContext context) {
        return new Class<?>[]{Default.class};
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
     * 一次增强长度
     * 用户可以自主覆盖
     *
     * @return 500
     */
    public Integer enhanceLength() {
        return 500;
    }

    @Override
    public void export(DownloaderRequestContext context) {
        // 创建workbook
        try (ExcelBean excelBean = ExcelExports.createWorkbook()) {
            List<Field> fieldList = ExcelBeanUtils
                .getFieldsByGroup(GenericUtils.getClassT(this, 2), exportGroup(context));
            // 设置表头header
            ExcelExports.writeHeader(excelBean, fieldList);

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
                            ExcelExports.writeData(excelBean, fieldList, enhance(tempList));
                            // 清除临时数据
                            tempList.clear();
                        }
                        tempList.add(t);
                    });
                if (!tempList.isEmpty()) {
                    ExcelExports.writeData(excelBean, fieldList, enhance(tempList));
                    // 清除临时数据
                    tempList.clear();
                }
                ExcelExports.writeWorkbook(excelBean, context.getOut());
            } finally {
                if (Objects.nonNull(session)) {
                    try {
                        session.close();
                    } catch (Exception ex) {
                    }
                }
                if (Objects.nonNull(iterable) && iterable instanceof Closeable) {
                    try {
                        ((Closeable) iterable).close();
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

}

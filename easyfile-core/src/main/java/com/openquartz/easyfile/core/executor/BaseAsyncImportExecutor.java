package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.BaseImportRequestContext;
import com.openquartz.easyfile.common.bean.Pair;
import java.util.List;

/**
 * 异步导入执行器
 * 实现类需要提供注解{@link com.openquartz.easyfile.core.annotations.FileImportExecutor}
 *
 * @param <T> 导入数据实体类
 * @author svnee
 */
public interface BaseAsyncImportExecutor<T> {

    /**
     * 批量处理导入数据
     *
     * @param dataList 数据列表
     * @param context 上下文
     * @return 失败的数据列表 (Pair: 失败的数据, 错误信息)
     */
    List<Pair<T, String>> importData(List<T> dataList, BaseImportRequestContext context);

    /**
     * 获取导入数据的Class类型
     * 用于解析文件
     *
     * @return class
     */
    Class<T> getDataClass();

    /**
     * 批次大小
     *
     * @return 批次大小
     */
    default int batchSize() {
        return 100;
    }
}

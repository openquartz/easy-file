package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.core.context.ImportContext;
import com.openquartz.easyfile.core.context.ImportDataContext;
import com.openquartz.easyfile.core.context.ImportProcessContext;
import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.entity.ImportProcessResult;
import com.openquartz.easyfile.core.entity.ProcessResult;
import com.openquartz.easyfile.core.entity.ValidationResult;

import java.util.List;

/**
 * 导入执行器接口
 * 定义导入处理的核心方法
 *
 * @author svnee
 */
public interface BaseImportExecutor<T> {

    /**
     * 是否支持异步导入
     * 默认支持异步导入
     *
     * @param context 导入上下文
     * @return 是否支持异步
     */
    default boolean enableAsync(ImportContext context) {
        return true;
    }

    /**
     * 获取分页大小
     * 默认1000条/页
     *
     * @return 分页大小
     */
    default int getPageSize() {
        return 1000;
    }

    /**
     * 是否支持断点续传
     * 默认支持
     *
     * @return 是否支持断点续传
     */
    default boolean supportResumable() {
        return true;
    }

    /**
     * 统一导入入口（默认同步直跑 process；生产由 AOP 短路）
     * <p>
     * key: 是否异步；value: 注册ID
     *
     * @param context 请求上下文
     * @return 异步标记与注册ID
     */
    default Pair<Boolean, Long> importResult(ImportRequestContext context) {
        ImportContext importContext = ImportContext.builder()
            .ossUrl(context.getSourceFileUrl())
            .fileName(context.getFileName())
            .fileType(context.getFileType())
            .businessUniqueKey(context.getBusinessUniqueKey())
            .executeParam(context.getExecuteParam())
            .registerId(context.getRegisterId())
            .importCode(context.getImportCode())
            .createBy(context.getOperator())
            .build();
        process(importContext);
        return Pair.of(Boolean.FALSE, context.getRegisterId());
    }

    /**
     * 异步导入完成回调
     *
     * @param result 处理结果
     * @param context 请求上下文
     */
    default void asyncCompleteCallback(ImportProcessResult result, ImportRequestContext context) {
    }

    /**
     * 导入处理
     * 执行完整的导入流程
     *
     * @param context 导入上下文
     * @return 导入处理结果
     */
    ImportProcessResult process(ImportContext context);

    /**
     * 数据验证
     * 验证导入数据的有效性
     *
     * @param dataContext 数据上下文
     * @return 验证结果
     */
    ValidationResult validate(ImportDataContext<T> dataContext);

    /**
     * 业务处理
     * 处理验证通过的数据
     *
     * @param data 数据列表
     * @param processContext 处理上下文
     * @return 处理结果
     */
    ProcessResult processBusiness(List<T> data, ImportProcessContext processContext);
}
package com.openquartz.easyfile.core.service;

import com.openquartz.easyfile.common.response.ImportResult;

/**
 * 幂等性导入管理器接口
 * 用于管理导入任务的幂等性，防止重复导入
 *
 * @author svnee
 */
public interface IdempotentImportManager {

    /**
     * 检查导入任务是否已执行
     *
     * @param importKey 导入唯一键
     * @return 是否已执行
     */
    boolean isExecuted(String importKey);

    /**
     * 标记导入任务为已执行
     *
     * @param importKey 导入唯一键
     * @param result 执行结果
     */
    void markAsExecuted(String importKey, ImportResult result);

    /**
     * 获取已执行的结果
     *
     * @param importKey 导入唯一键
     * @return 执行结果
     */
    ImportResult getExecutedResult(String importKey);
}

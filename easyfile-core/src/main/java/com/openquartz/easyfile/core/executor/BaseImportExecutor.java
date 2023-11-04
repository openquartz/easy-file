package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.BaseImportRequestContext;
import com.openquartz.easyfile.common.bean.ImportRequestContext;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.response.ImportResult;

import java.io.InputStream;

/**
 * 导入执行器
 * {@link com.openquartz.easyfile.core.annotations.FileImportExecutor}
 *
 * @author svnee
 */
public interface BaseImportExecutor {

    /**
     * 是否开启异步导入
     *
     * @param context 上下文请求对象
     * @return 开启异步
     */
    boolean enableAsync(BaseImportRequestContext context);

    /**
     * import source i/o stream
     * @param context context
     * @return i/o stream
     */
    InputStream importSource(BaseImportRequestContext context);

    /**
     * 导入数据逻辑
     *
     * @param context request context
     */
    void importData(ImportRequestContext context);

    /**
     * 导入数据逻辑
     * @param context context
     * @return 是否开启异步。value: 导入任务ID
     */
    default Pair<Boolean, Long> importDataResult(ImportRequestContext context) {
        importData(context);
        return Pair.of(Boolean.FALSE, null);
    }

    /**
     * 异步导入执行完成回调
     *
     * @param result result
     * @param context 请求上下文
     */
    default void asyncCompleteCallback(ImportResult result, BaseImportRequestContext context) {
    }

}

package org.svnee.easyfile.starter.executor;

import org.apache.commons.lang3.tuple.Pair;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.response.ExportResult;

/**
 * 下载模版
 * 实现类需要提供注解{@link org.svnee.easyfile.common.annotation.FileExportExecutor}
 *
 * @author svnee
 */
public interface BaseDownloadExecutor {

    /**
     * 是否开启异步下载
     *
     * @param context 上下文请求对象
     * @return 开启异步
     */
    boolean enableAsync(DownloaderRequestContext context);

    /**
     * 同步导出逻辑。用户实现
     * 只需实现,不需调用
     *
     * @param context context
     */
    void export(DownloaderRequestContext context);

    /**
     * 导出方法
     * 用于调用
     * 实现时是同步下载逻辑
     * <p>
     * 当{@link #enableAsync(DownloaderRequestContext)} 为true 时,
     * key: true, value: 为 registerId
     *
     * @param context 上下文请求对象
     * @return key:下载是否是异步, value:导出ID(异步下载文件注册ID)
     */
    default Pair<Boolean, Long> exportResult(DownloaderRequestContext context) {
        export(context);
        return Pair.of(Boolean.FALSE, null);
    }

    /**
     * 异步下载执行完成回调
     *
     * @param result result
     * @param context 请求上下文
     */
    default void asyncCompleteCallback(ExportResult result, BaseDownloaderRequestContext context) {
    }

}

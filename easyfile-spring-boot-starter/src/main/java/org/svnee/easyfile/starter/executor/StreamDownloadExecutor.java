package org.svnee.easyfile.starter.executor;

import java.io.Closeable;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;

/**
 * 流式导出支持
 *
 * @param <S> session 对象
 * @param <R> 对象结果集合
 * @param <T> T 导出对象
 * @author svnee
 */
public interface StreamDownloadExecutor<S extends Closeable, R extends Iterable<T>, T> extends BaseDownloadExecutor {

    /**
     * 返回会话
     *
     * @return S -session对象
     */
    S openSession();

    /**
     * 查询结果
     *
     * @param context context
     * @param session session会话
     * @return 流式查询结果
     */
    R streamQuery(S session, DownloaderRequestContext context);

}

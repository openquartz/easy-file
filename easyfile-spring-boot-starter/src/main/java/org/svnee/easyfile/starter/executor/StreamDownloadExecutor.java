package org.svnee.easyfile.starter.executor;

import java.io.Closeable;
import org.svnee.easyfile.common.constants.Constants;

/**
 * 流式导出支持
 *
 * @param <S> session 对象
 * @author svnee
 */
public interface StreamDownloadExecutor<S extends Closeable> extends BaseDownloadExecutor {

    /**
     * 返回会话
     *
     * @return S -session对象
     */
    S openSession();

    /**
     * 一次增强长度-buffer 到内存长度
     * 用户可以自主覆盖
     *
     * @return 500
     */
    default Integer enhanceLength() {
        return Constants.STREAM_EXPORT_ENHANCE_RECOMMEND_LEN;
    }
}

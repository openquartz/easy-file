package org.svnee.easyfile.starter.executor;

/**
 * 下载进度条
 *
 * @author svnee
 */
public interface BaseDownloadProgressBar {

    /**
     * 进度
     *
     * @param registerId 注册ID
     * @return 进度
     */
    default double process(Long registerId) {
        return 0.0;
    }

}

package com.openquartz.easyfile.core.intercept.listener;

/**
 * 开始下载监听器
 *
 * @author svnee
 **/
public interface DownloadStartListener {

    /**
     * 监听开始下载
     *
     * @param startEvent 开始下载事件
     */
    void listen(DownloadStartEvent startEvent);

}

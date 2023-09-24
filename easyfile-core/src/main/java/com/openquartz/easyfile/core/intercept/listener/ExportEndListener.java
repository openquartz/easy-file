package com.openquartz.easyfile.core.intercept.listener;

/**
 * 结束下载监听器
 *
 * @author snvee
 */
public interface ExportEndListener {

    /**
     * 监听器
     *
     * @param endEvent 结束事件
     */
    void listen(ExportEndEvent endEvent);

}

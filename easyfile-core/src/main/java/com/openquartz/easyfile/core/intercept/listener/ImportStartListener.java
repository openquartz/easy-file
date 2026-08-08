package com.openquartz.easyfile.core.intercept.listener;

/**
 * 开始导入监听器
 *
 * @author svnee
 */
public interface ImportStartListener {

    /**
     * 监听开始导入
     *
     * @param startEvent 开始导入事件
     */
    void listen(ImportStartEvent startEvent);

}

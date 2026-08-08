package com.openquartz.easyfile.core.intercept.listener;

/**
 * 结束导入监听器
 *
 * @author svnee
 */
public interface ImportEndListener {

    /**
     * 监听器
     *
     * @param endEvent 结束事件
     */
    void listen(ImportEndEvent endEvent);

}

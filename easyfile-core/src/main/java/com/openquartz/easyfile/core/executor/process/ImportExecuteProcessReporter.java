package com.openquartz.easyfile.core.executor.process;

/**
 * 导入执行进度上报器
 *
 * @author svnee
 */
public interface ImportExecuteProcessReporter {

    /**
     * 开始执行
     */
    default void start() {
    }

    /**
     * 上报执行进度
     *
     * @param executeProcess 执行进度
     * @param resumePosition 断点位置
     */
    void report(Integer executeProcess, Long resumePosition);
}

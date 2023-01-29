package org.svnee.easyfile.starter.executor.process;

/**
 * 执行进度
 *
 * @author svnee
 */
public interface ExecuteProcessReporter {

    /**
     * 开始执行
     */
    default void start() {

    }

    /**
     * 上报执行进度
     *
     * @param executeProcess 执行进度 (0~100)
     */
    void report(Integer executeProcess);

    /**
     * 执行完成
     */
    default void complete() {

    }
}

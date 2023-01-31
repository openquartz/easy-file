package org.svnee.easyfile.starter.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.starter.executor.process.ExecuteProcessReporter;

/**
 * 执行进度探针
 * 用于上报 异步下载时的进度。
 * 上报需要注意：由于使用{@link ThreadLocal} 做上下文上报器的传递。所以上报要求必须在执行器当前的主线程中调用{@link #report(Integer)}上报执行。
 *
 * @author svnee
 **/
@Slf4j
public final class ExecuteProcessProbe {

    private static final ThreadLocal<ExecuteProcessReporter> CURRENT_REPORTER = new NamedThreadLocal<>(
        "Current Execute-Process-Reporter");

    private ExecuteProcessProbe() {
    }

    /**
     * set reporter
     *
     * @param reporter reporter
     */
    protected static void setCurrentReporter(ExecuteProcessReporter reporter) {
        CURRENT_REPORTER.set(reporter);
    }

    /**
     * clear
     */
    protected static void clear() {
        try {
            if (CURRENT_REPORTER.get() != null) {
                CURRENT_REPORTER.remove();
            }
        } catch (Exception ex) {
            log.error("[ExecuteProcessProbe]clear-error!", ex);
        }
    }

    /**
     * 上报执行进度
     *
     * @param executeProcess 执行进度 数据范围(0~100)
     */
    public static void report(Integer executeProcess) {
        if (executeProcess <= 0 || CURRENT_REPORTER.get() == null) {
            return;
        }
        if (executeProcess > Constants.FULL_PROCESS) {
            executeProcess = Constants.FULL_PROCESS;
        }
        CURRENT_REPORTER.get().report(executeProcess);
    }

}

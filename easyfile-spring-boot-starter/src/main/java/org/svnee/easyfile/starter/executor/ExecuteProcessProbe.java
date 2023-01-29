package org.svnee.easyfile.starter.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.svnee.easyfile.starter.executor.process.ExecuteProcessReporter;

/**
 * 执行进度探针
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
     * @param executeProcess 执行进度
     */
    public static void report(Integer executeProcess) {
        if (CURRENT_REPORTER.get() == null) {
            throw new IllegalArgumentException("current reporter not exist!");
        }
        CURRENT_REPORTER.get().report(executeProcess);
    }

}

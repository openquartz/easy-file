package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.core.executor.process.ImportExecuteProcessReporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;

/**
 * 导入执行进度探针
 *
 * @author svnee
 */
@Slf4j
public final class ImportExecuteProcessProbe {

    private static final ThreadLocal<ImportExecuteProcessReporter> CURRENT_REPORTER =
        new NamedThreadLocal<>("Current Import-Execute-Process-Reporter");

    private ImportExecuteProcessProbe() {
    }

    protected static void setCurrentReporter(ImportExecuteProcessReporter reporter) {
        CURRENT_REPORTER.set(reporter);
    }

    protected static void clear() {
        try {
            if (CURRENT_REPORTER.get() != null) {
                CURRENT_REPORTER.remove();
            }
        } catch (Exception ex) {
            log.error("[ImportExecuteProcessProbe]clear-error!", ex);
        }
    }

    /**
     * 上报执行进度
     *
     * @param executeProcess 执行进度
     * @param resumePosition 断点位置
     */
    public static void report(Integer executeProcess, Long resumePosition) {
        if (executeProcess == null || executeProcess <= 0 || CURRENT_REPORTER.get() == null) {
            return;
        }
        if (executeProcess > Constants.FULL_PROCESS) {
            executeProcess = Constants.FULL_PROCESS;
        }
        CURRENT_REPORTER.get().report(executeProcess, resumePosition);
    }
}

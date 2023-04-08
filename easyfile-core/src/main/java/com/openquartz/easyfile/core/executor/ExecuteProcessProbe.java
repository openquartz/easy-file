package com.openquartz.easyfile.core.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.core.executor.process.ExecuteProcessReporter;

/**
 * 执行进度探针。导出执行器上报入口
 *
 * 用于上报 异步下载时的进度。
 * 上报需要注意：由于使用{@link ThreadLocal} 做上下文上报器的传递。所以上报要求必须在执行器当前的主线程中调用{@link #report(Integer)}上报执行。
 *
 * {@code
 * <pre>
 * @Component
 * @FileExportExecutor("StudentDownloadDemoExecutor")
 * public class StudentDownloadDemoExecutor extends AbstractDownloadExcel07Executor {
 *
 *     @Resource
 *     private StudentMapper studentMapper;
 *
 *     @Override
 *     public boolean enableAsync(BaseDownloaderRequestContext context) {
 *         return true;
 *     }
 *
 *     @Override
 *     public void export(DownloaderRequestContext context) {
 *          // do export to i/o stream
 *
 *          // report execute process
 *          ExecuteProcessProbe.report(executeProcess);
 *     }
 * }
 * <pre>
 * }
 * @author svnee
 **/
@Slf4j
public final class ExecuteProcessProbe {

    private static final ThreadLocal<ExecuteProcessReporter> CURRENT_REPORTER = new NamedThreadLocal<>(
        "Current Execute-Process-Reporter");

    private ExecuteProcessProbe() {
    }

    /**
     * set current reporter
     * 设置当前进度上报器
     *
     * @param reporter reporter 执行进度上报器
     */
    protected static void setCurrentReporter(ExecuteProcessReporter reporter) {
        CURRENT_REPORTER.set(reporter);
    }

    /**
     * 清空当前上报器
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

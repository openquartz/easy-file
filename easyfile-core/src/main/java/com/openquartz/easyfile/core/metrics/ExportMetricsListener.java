package com.openquartz.easyfile.core.metrics;

import com.openquartz.easyfile.common.bean.BaseExporterRequestContext;
import com.openquartz.easyfile.common.response.ExportResult;
import com.openquartz.easyfile.common.util.SpringContextUtil;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.core.executor.BaseExportExecutor;
import com.openquartz.easyfile.core.intercept.ExportExecutorInterceptor;
import com.openquartz.easyfile.core.intercept.InterceptorContext;
import com.openquartz.easyfile.metrics.api.constants.MetricsKeyConstants;
import com.openquartz.easyfile.metrics.api.reporter.MetricsReporter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * DownloadMetricsListener
 *
 * @author svnee
 */
@Slf4j
public class ExportMetricsListener implements ExportExecutorInterceptor {

    static {
        MetricsReporter.registerCounter(MetricsKeyConstants.ASYNC_INVOKE_DOWNLOAD_TIMER, new String[]{"type"},
            "easyfile download total count");

        MetricsReporter.registerHistogram(MetricsKeyConstants.ASYNC_INVOKE_DOWNLOAD_TIMER, new String[]{"type"},
            "easyfile download Latency Histogram Millis (ms)");
    }

    private static final String START_TIME_KEY = "startTimestamp";

    @Override
    public void beforeExecute(BaseExportExecutor executor, BaseExporterRequestContext context, Long registerId,
        InterceptorContext interceptorContext) {

        try {
            interceptorContext.set(START_TIME_KEY, LocalDateTime.now());

            FileExportExecutor exportExecutor = SpringContextUtil
                .getRealClass(executor)
                .getDeclaredAnnotation(FileExportExecutor.class);
            MetricsReporter
                .counterIncrement(MetricsKeyConstants.DOWNLOAD_COUNTER, new String[]{exportExecutor.value()});

            MetricsReporter.counterIncrement(MetricsKeyConstants.ASYNC_INVOKE_DOWNLOAD_TIMER,
                new String[]{exportExecutor.value()});
        } catch (Exception ex) {
            log.error("[DownloadMetricsListener#beforeExecute] record metrics error!", ex);
        }
    }

    @Override
    public void afterExecute(BaseExportExecutor executor, BaseExporterRequestContext context, ExportResult result,
        InterceptorContext interceptorContext) {

        try {
            FileExportExecutor exportExecutor = SpringContextUtil
                .getRealClass(executor)
                .getDeclaredAnnotation(FileExportExecutor.class);

            MetricsReporter.recordTime(MetricsKeyConstants.ASYNC_INVOKE_DOWNLOAD_TIMER,
                new String[]{exportExecutor.value()},
                Objects.requireNonNull(interceptorContext.get(START_TIME_KEY, LocalDateTime.class))
                    .until(LocalDateTime.now(), ChronoUnit.MILLIS));
        } catch (Exception ex) {
            log.error("[DownloadMetricsListener#afterExecute] record metrics error!", ex);
        }
    }
}

package com.openquartz.easyfile.core.metrics;

import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.core.intercept.listener.ExportEndEvent;
import com.openquartz.easyfile.core.intercept.listener.ExportEndListener;
import com.openquartz.easyfile.core.intercept.listener.ExportStartEvent;
import com.openquartz.easyfile.core.intercept.listener.ExportStartListener;
import com.openquartz.easyfile.metrics.api.constants.MetricsKeyConstants;
import com.openquartz.easyfile.metrics.api.reporter.MetricsReporter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MetricsListener
 *
 * @author svnee
 */
public class MetricsListener implements ExportStartListener, ExportEndListener {

    private final Map<String, LocalDateTime> downloadTimerMap = new ConcurrentHashMap<>();

    static {
        MetricsReporter.registerCounter(MetricsKeyConstants.DOWNLOAD_COUNTER, new String[]{"type"},
            "easyfile download total count");
        MetricsReporter.registerCounter(MetricsKeyConstants.ASYNC_DOWNLOAD_COUNTER, new String[]{"type"},
            "easyfile async download total count");
        MetricsReporter.registerCounter(MetricsKeyConstants.SYNC_DOWNLOAD_COUNTER, new String[]{"type"},
            "easyfile sync download total count");

        MetricsReporter.registerHistogram(MetricsKeyConstants.SYNC_DOWNLOAD_TIMER, new String[]{"type"},
            "download Latency Histogram Millis (ms)");
    }

    @Override
    public void listen(ExportStartEvent startEvent) {
        downloadTimerMap.put(startEvent.getDownloadTraceId(), LocalDateTime.now());

        FileExportExecutor exportExecutor = startEvent.getExecutor().getClass()
            .getDeclaredAnnotation(FileExportExecutor.class);
        MetricsReporter.counterIncrement(MetricsKeyConstants.DOWNLOAD_COUNTER, new String[]{exportExecutor.value()});
        if (startEvent.isEnableAsync()) {
            MetricsReporter.counterIncrement(MetricsKeyConstants.ASYNC_DOWNLOAD_COUNTER,
                new String[]{exportExecutor.value()});
        } else {
            MetricsReporter.counterIncrement(MetricsKeyConstants.SYNC_DOWNLOAD_COUNTER,
                new String[]{exportExecutor.value()});
        }
    }

    @Override
    public void listen(ExportEndEvent endEvent) {

        FileExportExecutor exportExecutor = endEvent.getExecutor().getClass()
            .getDeclaredAnnotation(FileExportExecutor.class);

        if (!endEvent.isEnableAsync()) {
            MetricsReporter.recordTime(MetricsKeyConstants.SYNC_DOWNLOAD_TIMER, new String[]{exportExecutor.value()},
                downloadTimerMap.get(endEvent.getDownloadTraceId()).until(LocalDateTime.now(), ChronoUnit.MILLIS));
        }

        downloadTimerMap.remove(endEvent.getDownloadTraceId());
    }
}

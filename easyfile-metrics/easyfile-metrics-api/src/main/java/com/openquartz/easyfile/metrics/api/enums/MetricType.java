package com.openquartz.easyfile.metrics.api.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Metric type.
 *
 * @author svnee
 */
@Getter
@RequiredArgsConstructor
public enum MetricType {

    /**
     * Counter metric type.
     */
    COUNTER,

    /**
     * Gauge metric type.
     */
    GAUGE,

    /**
     * Histogram metric type.
     */
    HISTOGRAM
}

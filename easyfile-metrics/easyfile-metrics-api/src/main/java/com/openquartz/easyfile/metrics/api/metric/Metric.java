package com.openquartz.easyfile.metrics.api.metric;

import com.openquartz.easyfile.metrics.api.enums.MetricType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Metric.
 *
 * @author svnee
 */
@Getter
@RequiredArgsConstructor
public final class Metric {

    private final MetricType type;

    private final String name;

    private final String document;

    private final List<String> labels;
}

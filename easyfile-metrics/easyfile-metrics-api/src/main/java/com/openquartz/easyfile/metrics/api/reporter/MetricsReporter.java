package com.openquartz.easyfile.metrics.api.reporter;

import com.openquartz.easyfile.metrics.api.metric.Metric;
import com.openquartz.easyfile.metrics.api.spi.MetricsRegister;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Metrics reporter.
 *
 * @author svnee
 */
public final class MetricsReporter {

    private static MetricsRegister metricsRegister;

    /**
     * Register.
     *
     * @param metricsRegister metrics register
     */
    public static void register(final MetricsRegister metricsRegister) {
        MetricsReporter.metricsRegister = metricsRegister;
    }

    /**
     * Register metrics.
     *
     * @param metrics metric collection
     */
    public static void registerMetrics(final Collection<Metric> metrics) {
        for (Metric metric : metrics) {
            switch (metric.getType()) {
                case COUNTER:
                    registerCounter(metric.getName(), getLabelNames(metric.getLabels()), metric.getDocument());
                    break;
                case GAUGE:
                    registerGauge(metric.getName(), getLabelNames(metric.getLabels()), metric.getDocument());
                    break;
                case HISTOGRAM:
                    registerHistogram(metric.getName(), getLabelNames(metric.getLabels()), metric.getDocument());
                    break;
                default:
                    throw new RuntimeException("we not support metric registration for type: " + metric.getType());
            }
        }
    }

    /**
     * Register counter.
     *
     * @param name name
     * @param labelNames label names
     * @param document document for counter
     */
    public static void registerCounter(final String name, final String[] labelNames, final String document) {
        Optional.ofNullable(metricsRegister)
            .ifPresent(register -> register.registerCounter(name, labelNames, document));
    }

    /**
     * Register counter.
     *
     * @param name name
     * @param document document for counter
     */
    public static void registerCounter(final String name, final String document) {
        registerCounter(name, null, document);
    }

    /**
     * Register gauge.
     *
     * @param name name
     * @param labelNames label names
     * @param document document for gauge
     */
    public static void registerGauge(final String name, final String[] labelNames, final String document) {
        Optional.ofNullable(metricsRegister).ifPresent(register -> register.registerGauge(name, labelNames, document));
    }

    /**
     * Register gauge.
     *
     * @param name name
     * @param document document for gauge
     */
    public static void registerGauge(final String name, final String document) {
        registerGauge(name, null, document);
    }

    /**
     * Register histogram by label names.
     *
     * @param name name
     * @param labelNames label names
     * @param document document for histogram
     */
    public static void registerHistogram(final String name, final String[] labelNames, final String document) {
        Optional.ofNullable(metricsRegister)
            .ifPresent(register -> register.registerHistogram(name, labelNames, document));
    }

    /**
     * Register histogram.
     *
     * @param name name
     * @param document document for histogram
     */
    public static void registerHistogram(final String name, final String document) {
        registerHistogram(name, null, document);
    }

    /**
     * Counter increment.
     *
     * @param name name
     * @param labelValues label values
     */
    public static void counterIncrement(final String name, final String[] labelValues) {
        counterIncrement(name, labelValues, 1);
    }

    /**
     * Counter increment.
     *
     * @param name name
     */
    public static void counterIncrement(final String name) {
        counterIncrement(name, null, 1);
    }

    /**
     * Counter increment by count.
     *
     * @param name name
     * @param labelValues label values
     * @param count count
     */
    public static void counterIncrement(final String name, final String[] labelValues, final long count) {
        Optional.ofNullable(metricsRegister).ifPresent(register -> register.counterIncrement(name, labelValues, count));
    }

    /**
     * Gauge increment.
     *
     * @param name name
     * @param labelValues label values
     */
    public static void gaugeIncrement(final String name, final String[] labelValues) {
        Optional.ofNullable(metricsRegister).ifPresent(register -> register.gaugeIncrement(name, labelValues));
    }

    /**
     * Gauge increment.
     *
     * @param name name
     */
    public static void gaugeIncrement(final String name) {
        gaugeIncrement(name, null);
    }

    /**
     * Gauge decrement.
     *
     * @param name name
     * @param labelValues label values
     */
    public static void gaugeDecrement(final String name, final String[] labelValues) {
        Optional.ofNullable(metricsRegister).ifPresent(register -> register.gaugeDecrement(name, labelValues));
    }

    /**
     * Gauge decrement.
     *
     * @param name name
     */
    public static void gaugeDecrement(final String name) {
        gaugeDecrement(name, null);
    }

    /**
     * Record time by duration.
     *
     * @param name name
     * @param labelValues label values
     * @param duration duration
     */
    public static void recordTime(final String name, final String[] labelValues, final long duration) {
        Optional.ofNullable(metricsRegister).ifPresent(register -> register.recordTime(name, labelValues, duration));
    }

    /**
     * Record time by duration.
     *
     * @param name name
     * @param duration duration
     */
    public static void recordTime(final String name, final long duration) {
        recordTime(name, null, duration);
    }

    private static String[] getLabelNames(final List<String> labels) {
        return labels.toArray(new String[0]);
    }
}

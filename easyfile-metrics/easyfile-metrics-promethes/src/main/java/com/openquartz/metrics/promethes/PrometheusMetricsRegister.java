package com.openquartz.metrics.promethes;

import com.openquartz.easyfile.common.spi.SPI;
import com.openquartz.easyfile.metrics.api.spi.MetricsRegister;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prometheus metrics register.
 *
 * @author svnee
 */
@Slf4j
@SPI("prometheus")
public final class PrometheusMetricsRegister implements MetricsRegister {

    private static final Map<String, Counter> COUNTER_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Gauge> GAUGE_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Histogram> HISTOGRAM_MAP = new ConcurrentHashMap<>();

    /**
     * Get instance prometheus metrics register.
     *
     * @return the prometheus metrics register
     */
    public static PrometheusMetricsRegister getInstance() {
        return PrometheusMetricsRegisterHolder.INSTANCE;
    }

    @Override
    public void registerCounter(final String name, final String[] labelNames, final String document) {
        if (!COUNTER_MAP.containsKey(name)) {
            Counter.Builder builder = Counter.build().name(name).help(document);
            if (null != labelNames) {
                builder.labelNames(labelNames);
            }
            COUNTER_MAP.put(name, builder.register());
        }
    }

    @Override
    public void registerGauge(final String name, final String[] labelNames, final String document) {
        if (!GAUGE_MAP.containsKey(name)) {
            Gauge.Builder builder = Gauge.build().name(name).help(document);
            if (null != labelNames) {
                builder.labelNames(labelNames);
            }
            GAUGE_MAP.put(name, builder.register());
        }
    }

    @Override
    public void registerHistogram(final String name, final String[] labelNames, final String document) {
        if (!HISTOGRAM_MAP.containsKey(name)) {
            Histogram.Builder builder = Histogram.build().name(name).help(document);
            if (null != labelNames) {
                builder.labelNames(labelNames);
            }
            HISTOGRAM_MAP.put(name, builder.register());
        }
    }

    @Override
    public void counterIncrement(final String name, final String[] labelValues, final long count) {
        Counter counter = COUNTER_MAP.get(name);
        if (null != labelValues) {
            counter.labels(labelValues).inc(count);
        } else {
            counter.inc(count);
        }
    }

    @Override
    public void gaugeIncrement(final String name, final String[] labelValues) {
        Gauge gauge = GAUGE_MAP.get(name);
        if (null != labelValues) {
            gauge.labels(labelValues).inc();
        } else {
            gauge.inc();
        }
    }

    @Override
    public void gaugeDecrement(final String name, final String[] labelValues) {
        Gauge gauge = GAUGE_MAP.get(name);
        if (null != labelValues) {
            gauge.labels(labelValues).dec();
        } else {
            gauge.dec();
        }
    }

    @Override
    public void recordTime(final String name, final String[] labelValues, final long duration) {
        Histogram histogram = HISTOGRAM_MAP.get(name);
        if (null != labelValues) {
            histogram.labels(labelValues).observe(duration);
        } else {
            histogram.observe(duration);
        }
    }

    private static class PrometheusMetricsRegisterHolder {

        private static final PrometheusMetricsRegister INSTANCE = new PrometheusMetricsRegister();
    }
}

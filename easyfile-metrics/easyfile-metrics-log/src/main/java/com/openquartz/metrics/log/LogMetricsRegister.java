package com.openquartz.metrics.log;

import com.openquartz.easyfile.metrics.api.spi.MetricsRegister;
import lombok.extern.slf4j.Slf4j;

/**
 * LogMetricsRegister
 *
 * @author svnee
 */
@Slf4j
public class LogMetricsRegister implements MetricsRegister {

    @Override
    public void registerGauge(String name, String[] labelNames, String document) {
        // TODO: 2023/4/16
    }

    @Override
    public void registerCounter(String name, String[] labelNames, String document) {
        // TODO: 2023/4/16
    }

    @Override
    public void registerHistogram(String name, String[] labelNames, String document) {
        // TODO: 2023/4/16

    }

    @Override
    public void counterIncrement(String name, String[] labelValues, long count) {
        // TODO: 2023/4/16

    }

    @Override
    public void gaugeIncrement(String name, String[] labelValues) {
        // TODO: 2023/4/16

    }

    @Override
    public void gaugeDecrement(String name, String[] labelValues) {
        // TODO: 2023/4/16

    }

    @Override
    public void recordTime(String name, String[] labelValues, long duration) {
        // TODO: 2023/4/16

    }
}

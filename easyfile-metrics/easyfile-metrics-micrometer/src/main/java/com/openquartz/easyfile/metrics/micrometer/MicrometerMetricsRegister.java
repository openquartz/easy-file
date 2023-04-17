package com.openquartz.easyfile.metrics.micrometer;

import com.openquartz.easyfile.common.spi.SPI;
import com.openquartz.easyfile.metrics.api.spi.MetricsRegister;

/**
 * Micrometer Metrics RegisterSupport
 *
 * @author svnee
 */
@SPI("micrometer")
public class MicrometerMetricsRegister implements MetricsRegister {

    @Override
    public void registerGauge(String name, String[] labelNames, String document) {

    }

    @Override
    public void registerCounter(String name, String[] labelNames, String document) {

    }

    @Override
    public void registerHistogram(String name, String[] labelNames, String document) {

    }

    @Override
    public void counterIncrement(String name, String[] labelValues, long count) {

    }

    @Override
    public void gaugeIncrement(String name, String[] labelValues) {

    }

    @Override
    public void gaugeDecrement(String name, String[] labelValues) {

    }

    @Override
    public void recordTime(String name, String[] labelValues, long duration) {

    }
}

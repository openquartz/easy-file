package com.openquartz.easyfile.metrics.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * MicrometerMetrics
 *
 * @author svnee
 */
public class MicrometerMetrics implements MeterBinder, AutoCloseable {

    @Override
    public void bindTo(MeterRegistry registry) {

    }

    @Override
    public void close() throws Exception {

    }
}

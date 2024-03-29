/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openquartz.easyfile.metrics.api.spi;

/**
 * Metrics register.
 *
 * @author svnee
 */
public interface MetricsRegister {

    /**
     * Register gauge.
     *
     * @param name name
     * @param labelNames label names
     * @param document document for gauge
     */
    void registerGauge(String name, String[] labelNames, String document);

    /**
     * Register counter.
     *
     * @param name name
     * @param labelNames label names
     * @param document document for counter
     */
    void registerCounter(String name, String[] labelNames, String document);

    /**
     * Register histogram.
     *
     * @param name name
     * @param labelNames label names
     * @param document document for histogram
     */
    void registerHistogram(String name, String[] labelNames, String document);

    /**
     * Counter increment by count.
     *
     * @param name name
     * @param labelValues label values
     * @param count count
     */
    void counterIncrement(String name, String[] labelValues, long count);

    /**
     * Gauge increment.
     *
     * @param name name
     * @param labelValues label values
     */
    void gaugeIncrement(String name, String[] labelValues);

    /**
     * Gauge decrement.
     *
     * @param name name
     * @param labelValues label values
     */
    void gaugeDecrement(String name, String[] labelValues);

    /**
     * Record time by duration.
     *
     * @param name name
     * @param labelValues label values
     * @param duration duration
     */
    void recordTime(String name, String[] labelValues, long duration);
}

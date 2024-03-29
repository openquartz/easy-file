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

import com.openquartz.easyfile.metrics.api.config.MetricsConfig;

/**
 * Metrics tracker manager.
 *
 * @author svnee
 */
public interface MetricsBootService {

    /**
     * Start metrics tracker.
     *
     * @param metricsConfig metrics config
     * @param register the register
     */
    void start(MetricsConfig metricsConfig, MetricsRegister register);

    /**
     * Stop metrics tracker.
     */
    void stop();
}


package com.openquartz.metrics.promethes;

import com.openquartz.easyfile.common.spi.SPI;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.metrics.api.config.MetricsConfig;
import com.openquartz.easyfile.metrics.api.reporter.MetricsReporter;
import com.openquartz.easyfile.metrics.api.spi.MetricsBootService;
import com.openquartz.easyfile.metrics.api.spi.MetricsRegister;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.jmx.BuildInfoCollector;
import io.prometheus.jmx.JmxCollector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Prometheus metrics tracker manager.
 *
 * @author svnee
 */
@Getter
@Slf4j
@SPI("prometheus")
public final class PrometheusMetricsBootService implements MetricsBootService {

    private HTTPServer server;

    private final AtomicBoolean registered = new AtomicBoolean(false);

    @Override
    public void start(final MetricsConfig metricsConfig, final MetricsRegister register) {
        startServer(metricsConfig);
        MetricsReporter.register(register);
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
            registered.set(false);
            CollectorRegistry.defaultRegistry.clear();
        }
    }

    private void startServer(final MetricsConfig metricsConfig) {
        register(metricsConfig.getJmxConfig());
        int port = metricsConfig.getPort();
        String host = metricsConfig.getHost();
        InetSocketAddress inetSocketAddress;
        if (null == host || "".equalsIgnoreCase(host)) {
            inetSocketAddress = new InetSocketAddress(port);
        } else {
            inetSocketAddress = new InetSocketAddress(host, port);
        }
        try {
            server = new HTTPServer(inetSocketAddress, CollectorRegistry.defaultRegistry, true);
            log.info(String.format("Prometheus metrics HTTP server `%s:%s` start success.",
                inetSocketAddress.getHostString(), inetSocketAddress.getPort()));
        } catch (final IOException ex) {
            log.error("Prometheus metrics HTTP server start fail", ex);
        }
    }

    private void register(final String jmxConfig) {
        if (!registered.compareAndSet(false, true)) {
            return;
        }
        new BuildInfoCollector().register();
        DefaultExports.initialize();
        try {
            if (StringUtils.isNotBlank(jmxConfig)) {
                new JmxCollector(jmxConfig).register();
            }
        } catch (MalformedObjectNameException e) {
            log.error("init jmx collector error", e);
        }
    }
}


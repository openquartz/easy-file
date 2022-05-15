package org.svnee.easyfile.server.common.spi;

import java.util.List;

/**
 * @author svnee
 */
public class SpiSupport {

    private SpiSupport() {
    }

    private static final ServiceRegistry REGISTRY = new ServiceRegistry();

    public static <T extends ServiceProvider> void register(Class<? extends T> clazz, String providerName, T provider) {
        REGISTRY.register(clazz, providerName, provider);
    }

    public static <T extends ServiceProvider> T getServiceProvider(Class<T> clazz, String providerName) {
        return (T) REGISTRY.getServiceProvider(clazz, providerName);
    }

    public static <T extends ServiceProvider> List<T> getServiceProviders(Class<T> clazz) {
        return REGISTRY.getServiceProviders(clazz);
    }
}

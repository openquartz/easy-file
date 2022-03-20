package org.svnee.easyfile.server.common.spi;

import java.util.List;

/**
 * @author svnee
 */
public class SpiSupport {

    private SpiSupport() {
    }

    private static ServiceRegistry registry = new ServiceRegistry();

    public static <T extends ServiceProvider> void register(Class<? extends T> clazz, String providerName, T provider) {
        registry.register(clazz, providerName, provider);
    }

    public static <T extends ServiceProvider> T getServiceProvider(Class<T> clazz, String providerName) {
        T provider = (T) registry.getServiceProvider(clazz, providerName);
        return provider;
    }

    public static <T extends ServiceProvider> List getServiceProviders(Class<T> clazz) {
        return registry.getServiceProviders(clazz);
    }
}

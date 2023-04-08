package com.openquartz.easyfile.server.common.spi;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openquartz.easyfile.common.util.CollectionUtils;

/**
 * @author svnee
 */
public class ServiceRegistry {

    private final Map<Class<? extends ServiceProvider>, Map<String, ServiceProvider>> registerMap = new ConcurrentHashMap<>();

    public void register(Class<? extends ServiceProvider> clazz, String providerName, ServiceProvider serviceProvider) {
        if (!registerMap.containsKey(clazz)) {
            registerMap.putIfAbsent(clazz, new ConcurrentHashMap<>());
        }
        Map<String, ServiceProvider> map = registerMap.get(clazz);
        if (!map.containsKey(providerName)) {
            map.putIfAbsent(providerName, serviceProvider);
        }
    }

    public ServiceProvider getServiceProvider(Class<? extends ServiceProvider> clazz, String providerName) {
        if (!registerMap.containsKey(clazz)) {
            return null;
        }
        Map<String, ServiceProvider> providerMap = registerMap.get(clazz);
        return providerMap.get(providerName);
    }

    public List getServiceProviders(Class<? extends ServiceProvider> clazz) {
        if (!registerMap.containsKey(clazz)) {
            return CollectionUtils.newArrayList();
        }
        Map<String, ServiceProvider> providerMap = registerMap.get(clazz);
        return CollectionUtils.newArrayList(providerMap.values());
    }
}

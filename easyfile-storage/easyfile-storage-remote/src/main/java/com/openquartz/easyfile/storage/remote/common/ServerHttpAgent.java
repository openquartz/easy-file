package com.openquartz.easyfile.storage.remote.common;

import java.util.Map;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.util.SpringContextUtil;
import com.openquartz.easyfile.common.util.json.TypeReference;

/**
 * Server http agent.
 *
 * @author svnee
 */
public class ServerHttpAgent implements HttpAgent {

    private final RemoteBootstrapProperties remoteBootstrapProperties;

    private final ServerListManager serverListManager;

    private final RemoteClient remoteClient;

    private ServerHealthCheck serverHealthCheck;

    public ServerHttpAgent(RemoteBootstrapProperties properties, RemoteClient remoteClient) {
        this.remoteBootstrapProperties = properties;
        this.remoteClient = remoteClient;
        this.serverListManager = new ServerListManager(remoteBootstrapProperties);
    }

    @Override
    public void start() {

    }

    @Override
    public String getTenantId() {
        return remoteBootstrapProperties.getNamespace();
    }

    @Override
    public String getEncode() {
        return null;
    }

    @Override
    public ResponseResult<?> httpGetSimple(String path) {
        return remoteClient.restApiGetHealth(buildUrl(path), ResponseResult.class);
    }

    @Override
    public <T>ResponseResult<T> httpPost(String path, Object body,Class<T> bodyClazz) {
        isHealthStatus();
        return remoteClient.restApiPost(buildUrl(path), body, new TypeReference<ResponseResult<T>>() {
        });
    }

    @Override
    public <T> ResponseResult<T> httpPost(String path, Object body, TypeReference<ResponseResult<T>> reference) {
        isHealthStatus();
        return remoteClient.restApiPost(buildUrl(path), body, reference);
    }

    @Override
    public ResponseResult<?> httpPost(String path, Object body) {
        isHealthStatus();
        return remoteClient.restApiPost(buildUrl(path), body, ResponseResult.class);
    }

    @Override
    public ResponseResult<?> httpPostByDiscovery(String path, Object body) {
        isHealthStatus();
        return remoteClient.restApiPost(buildUrl(path), body, ResponseResult.class);
    }

    @Override
    public <T> ResponseResult<T> httpPostByDiscovery(String path, Object body, Class<T> bodyClazz) {
        isHealthStatus();
        return remoteClient.restApiPost(buildUrl(path), body, new TypeReference<ResponseResult<T>>() {
        });
    }

    @Override
    public ResponseResult<?> httpGetByConfig(String path, Map<String, String> headers, Map<String, String> paramValues, long readTimeoutMs) {
        isHealthStatus();
        return remoteClient.restApiGetByThreadPool(buildUrl(path), headers, paramValues, readTimeoutMs, ResponseResult.class);
    }

    @Override
    public ResponseResult<?> httpPostByConfig(String path, Map<String, String> headers, Map<String, String> paramValues, long readTimeoutMs) {
        isHealthStatus();
        return remoteClient.restApiPostByThreadPool(buildUrl(path), headers, paramValues, readTimeoutMs, ResponseResult.class);
    }

    private String buildUrl(String path) {
        return serverListManager.getCurrentServerAddr() + path;
    }

    private void isHealthStatus() {
        if (serverHealthCheck == null) {
            serverHealthCheck = SpringContextUtil.getBean(ServerHealthCheck.class);
        }

        serverHealthCheck.isHealthStatus();
    }


}

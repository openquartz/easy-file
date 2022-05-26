package org.svnee.easyfile.storage.remote;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.thread.ThreadFactoryBuilder;
import org.svnee.easyfile.common.util.MapUtils;
import org.svnee.easyfile.common.util.SpringContextUtil;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.common.util.TypeReference;

/**
 * Server http agent.
 *
 * @author svnee
 */
public class ServerHttpAgent implements HttpAgent {

    private final RemoteBootstrapProperties remoteBootstrapProperties;

    private final ServerListManager serverListManager;

    private final RemoteClient remoteClient;

    private final SecurityProxy securityProxy;

    private ServerHealthCheck serverHealthCheck;

    public ServerHttpAgent(RemoteBootstrapProperties properties, RemoteClient remoteClient) {
        this.remoteBootstrapProperties = properties;
        this.remoteClient = remoteClient;
        this.serverListManager = new ServerListManager(remoteBootstrapProperties);
        this.securityProxy = new SecurityProxy(remoteClient, properties);

        this.securityProxy.applyToken(this.serverListManager.getServerUrls());
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(
            1,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("SecurityInfoRefresh-thread-%d").build()
        );

        long securityInfoRefreshIntervalMills = TimeUnit.SECONDS.toMillis(5);
        executorService.scheduleWithFixedDelay(
                () -> securityProxy.applyToken(serverListManager.getServerUrls()),
                0,
            securityInfoRefreshIntervalMills,
                TimeUnit.MILLISECONDS
        );
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
        path = injectSecurityInfoByPath(path);
        return remoteClient.restApiGetHealth(buildUrl(path), ResponseResult.class);
    }

    @Override
    public <T>ResponseResult<T> httpPost(String path, Object body,Class<T> bodyClazz) {
        isHealthStatus();
        path = injectSecurityInfoByPath(path);
        return remoteClient.restApiPost(buildUrl(path), body, new TypeReference<ResponseResult<T>>() {
        });
    }

    @Override
    public <T> ResponseResult<T> httpPost(String path, Object body, TypeReference<ResponseResult<T>> reference) {
        isHealthStatus();
        path = injectSecurityInfoByPath(path);
        return remoteClient.restApiPost(buildUrl(path), body, reference);
    }

    @Override
    public ResponseResult<?> httpPost(String path, Object body) {
        isHealthStatus();
        path = injectSecurityInfoByPath(path);
        return remoteClient.restApiPost(buildUrl(path), body, ResponseResult.class);
    }

    @Override
    public ResponseResult<?> httpPostByDiscovery(String path, Object body) {
        isHealthStatus();
        path = injectSecurityInfoByPath(path);
        return remoteClient.restApiPost(buildUrl(path), body, ResponseResult.class);
    }

    @Override
    public <T> ResponseResult<T> httpPostByDiscovery(String path, Object body, Class<T> bodyClazz) {
        isHealthStatus();
        path = injectSecurityInfoByPath(path);
        return remoteClient.restApiPost(buildUrl(path), body, new TypeReference<ResponseResult<T>>() {
        });
    }

    @Override
    public ResponseResult<?> httpGetByConfig(String path, Map<String, String> headers, Map<String, String> paramValues, long readTimeoutMs) {
        isHealthStatus();
        injectSecurityInfo(paramValues);
        return remoteClient.restApiGetByThreadPool(buildUrl(path), headers, paramValues, readTimeoutMs, ResponseResult.class);
    }

    @Override
    public ResponseResult<?> httpPostByConfig(String path, Map<String, String> headers, Map<String, String> paramValues, long readTimeoutMs) {
        isHealthStatus();
        injectSecurityInfo(paramValues);
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

    private Map<String,String> injectSecurityInfo(Map<String, String> params) {
        if (StringUtils.isNotBlank(securityProxy.getAccessToken())) {
            params.put(Constants.ACCESS_TOKEN, securityProxy.getAccessToken());
        }

        return params;
    }

    private String injectSecurityInfoByPath(String path) {
        return remoteClient.buildUrl(path, injectSecurityInfo(MapUtils.newHashMap()));
    }

}

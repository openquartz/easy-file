package org.svnee.easyfile.storage.remote;

import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.common.util.TypeReference;

/**
 * Security proxy.
 *
 * @author svnee
 * @date 2021/12/20 20:19
 */
@Slf4j
public class SecurityProxy {

    private static final String APPLY_TOKEN_URL = Constants.BASE_PATH + "/auth/users/apply/token";

    private final RemoteClient remoteClient;

    private final String username;

    private final String password;

    private String accessToken;

    private long tokenTtl;

    private long lastRefreshTime;

    private long tokenRefreshWindow;

    public SecurityProxy(RemoteClient remoteClient, RemoteBootstrapProperties properties) {
        username = properties.getUsername();
        password = properties.getPassword();
        this.remoteClient = remoteClient;
    }

    public boolean applyToken(List<String> servers) {
        try {
            if ((System.currentTimeMillis() - lastRefreshTime) < TimeUnit.SECONDS
                    .toMillis(tokenTtl - tokenRefreshWindow)) {
                return true;
            }

            for (String server : servers) {
                if (applyToken(server)) {
                    lastRefreshTime = System.currentTimeMillis();
                    return true;
                }
            }
        } catch (Throwable ignore) {
            // ignore
        }

        return false;
    }

    public boolean applyToken(String server) {
        if (StringUtils.isAllNotEmpty(username, password)) {
            String url = server + APPLY_TOKEN_URL;

            Map<String, String> bodyMap = new HashMap<>(2);
            bodyMap.put("userName", username);
            bodyMap.put("password", password);

            try {
                ResponseResult<String> responseResult = remoteClient.restApiPost(url, bodyMap,
                    new TypeReference<ResponseResult<String>>() {
                    });
                if (!responseResult.isSuccess()) {
                    log.error("Error getting access token. message :: {}", responseResult.getMessage());
                    return false;
                }

                String tokenJsonStr = JSONUtil.toJson(responseResult.getData());
                TokenInfo tokenInfo = JSONUtil.parseObject(tokenJsonStr, TokenInfo.class);

                accessToken = tokenInfo.getAccessToken();
                tokenTtl = tokenInfo.getTokenTtl();
                tokenRefreshWindow = tokenTtl / 10;
            } catch (Throwable ex) {
                log.error("Failed to apply for token. message :: {}", ex.getMessage());
                return false;
            }
        }

        return true;
    }

    public String getAccessToken() {
        return accessToken;
    }

}

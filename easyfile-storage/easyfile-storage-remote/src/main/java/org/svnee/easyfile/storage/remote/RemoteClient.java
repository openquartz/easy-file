package org.svnee.easyfile.storage.remote;

import static org.svnee.easyfile.storage.exception.RemoteExceptionCode.HTTP_RESPONSE_BODY_NULL_ERROR;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.util.CollectionUtils;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.common.util.json.TypeReference;
import org.svnee.easyfile.storage.exception.RemoteExceptionCode;

/**
 * HttpClient util.
 *
 * @author svnee
 */
@Slf4j
public class RemoteClient {

    private final OkHttpClient okHttpClient;

    private final MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");

    private static final int HTTP_OK_CODE = 200;

    public RemoteClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * Get 请求.
     *
     * @param url url
     * @return getResult
     */
    @SneakyThrows
    public String get(String url) {
        try {
            return new String(doGet(url), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("httpGet 调用失败. {}", url, e);
            throw e;
        }
    }

    /**
     * Get 请求, 支持添加查询字符串.
     *
     * @param queryString 查询字符串
     */
    public String get(String url, Map<String, String> queryString) {
        String fullUrl = buildUrl(url, queryString);
        return get(fullUrl);
    }

    /**
     * 获取 Json 后直接反序列化.
     */
    public <T> T restApiGet(String url, Class<T> clazz) {
        String resp = get(url);
        return JSONUtil.parseObject(resp, clazz);
    }

    /**
     * 调用健康检查.
     */
    @SneakyThrows
    public <T> T restApiGetHealth(String url, Class<T> clazz) {
        String resp = new String(doGet(url), StandardCharsets.UTF_8);
        return JSONUtil.parseObject(resp, clazz);
    }

    /**
     * Get 请求, 支持查询字符串.
     */
    public <T> T restApiGet(String url, Map<String, String> queryString, Class<T> clazz) {
        String fullUrl = buildUrl(url, queryString);
        String resp = get(fullUrl);
        return JSONUtil.parseObject(resp, clazz);
    }

    /**
     * Rest 接口 Post 调用.
     */
    public String restApiPost(String url, Object body) {
        try {
            return doPost(url, body);
        } catch (Exception e) {
            log.error("httpPost 调用失败. {} message :: {}", url, e.getMessage());
            throw e;
        }
    }

    /**
     * Rest 接口 Post 调用.
     * 对返回值直接反序列化.
     */
    public <T> T restApiPost(String url, Object body, Class<T> clazz) {
        String resp = restApiPost(url, body);
        return JSONUtil.parseObject(resp, clazz);
    }

    /**
     * Rest 接口 Post 调用.
     * 对返回值直接反序列化.
     */
    public <T> T restApiPost(String url, Object body, TypeReference<T> reference) {
        String resp = restApiPost(url, body);
        return JSONUtil.parseObject(resp, reference);
    }

    /**
     * 根据查询字符串构造完整的 Url.
     */
    public String buildUrl(String url, Map<String, String> queryString) {
        if (null == queryString) {
            return url;
        }

        StringBuilder builder = new StringBuilder(url);
        boolean isFirst = true;

        for (Map.Entry<String, String> entry : queryString.entrySet()) {
            String key = entry.getKey();
            if (key != null && entry.getValue() != null) {
                if (isFirst) {
                    isFirst = false;
                    builder.append("?");
                } else {
                    builder.append("&");
                }
                builder.append(key)
                    .append("=")
                    .append(queryString.get(key));
            }
        }

        return builder.toString();
    }

    @SneakyThrows
    private String doPost(String url, Object body) {
        String jsonBody = JSONUtil.toJson(body);
        RequestBody requestBody = RequestBody.create(jsonMediaType, jsonBody);
        Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .build();
        try (Response resp = okHttpClient.newCall(request).execute()) {
            try (ResponseBody responseBody = resp.body()) {
                if (resp.code() != HTTP_OK_CODE) {
                    String msg = String
                        .format("HttpPost 响应 code 异常. [code] %s [url] %s [body] %s", resp.code(), url, jsonBody);
                    throw new EasyFileException(RemoteExceptionCode.HTTP_ERROR, msg);
                }

                Asserts.notNull(responseBody, HTTP_RESPONSE_BODY_NULL_ERROR);
                return responseBody.string();
            }
        }
    }

    @SneakyThrows
    private byte[] doGet(String url) {
        Request request = new Request.Builder().get().url(url).build();
        try (Response resp = okHttpClient.newCall(request).execute()) {
            try (ResponseBody responseBody = resp.body()) {
                if (resp.code() != HTTP_OK_CODE) {
                    String msg = String.format("HttpGet 响应 code 异常. [code] %s [url] %s", resp.code(), url);
                    throw new EasyFileException(RemoteExceptionCode.HTTP_ERROR, msg);
                }
                Asserts.notNull(responseBody, HTTP_RESPONSE_BODY_NULL_ERROR);
                return responseBody.bytes();
            }
        }
    }

    @SneakyThrows
    public <T> T restApiGetByThreadPool(String url, Map<String, String> headers, Map<String, String> paramValues,
        Long readTimeoutMs, Class<T> clazz) {
        String buildUrl = buildUrl(url, paramValues);
        Request.Builder builder = new Request.Builder().get();

        if (!CollectionUtils.isEmpty(headers)) {
            builder.headers(Headers.of(headers));
        }

        Request request = builder.url(buildUrl).build();

        Call call = okHttpClient.newCall(request);
        call.timeout().timeout(readTimeoutMs, TimeUnit.MILLISECONDS);

        try (Response resp = call.execute()) {
            try (ResponseBody responseBody = resp.body()) {
                if (resp.code() != HTTP_OK_CODE) {
                    String msg = String.format("HttpGet 响应 code 异常. [code] %s [url] %s", resp.code(), url);
                    log.error(msg);
                    throw new EasyFileException(RemoteExceptionCode.HTTP_ERROR, msg);
                }

                Asserts.notNull(responseBody, HTTP_RESPONSE_BODY_NULL_ERROR);
                return JSONUtil.parseObject(responseBody.string(), clazz);
            }
        }
    }

    @SneakyThrows
    public <T> T restApiPostByThreadPool(String url, Map<String, String> headers, Map<String, String> paramValues,
        Long readTimeoutMs, Class<T> clazz) {
        String buildUrl = buildUrl(url, paramValues);

        Request request = new Request.Builder()
            .url(buildUrl)
            .headers(Headers.of(headers))
            .post(RequestBody.create(jsonMediaType, StringUtils.EMPTY))
            .build();

        Call call = okHttpClient.newCall(request);
        call.timeout().timeout(readTimeoutMs, TimeUnit.MILLISECONDS);

        try (Response resp = call.execute()) {
            try (ResponseBody responseBody = resp.body()) {
                if (resp.code() != HTTP_OK_CODE) {
                    String msg = String.format("HttpPost 响应 code 异常. [code] %s [url] %s.", resp.code(), url);
                    log.error(msg);
                    throw new EasyFileException(RemoteExceptionCode.HTTP_ERROR, msg);
                }

                Asserts.notNull(responseBody, HTTP_RESPONSE_BODY_NULL_ERROR);
                return JSONUtil.parseObject(responseBody.string(), clazz);
            }
        }
    }

}

package org.svnee.easyfile.storage.remote;

import java.util.Map;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.util.json.TypeReference;

/**
 * Http agent.
 *
 * @author svnee
 */
public interface HttpAgent {

    /**
     * Start.
     */
    void start();

    /**
     * Get tenant id.
     * @return tenantId
     */
    String getTenantId();

    /**
     * Get encode.
     * @return encode
     */
    String getEncode();

    /**
     * Http get simple.
     * @param path path
     * @return response
     */
    ResponseResult<?> httpGetSimple(String path);

    /**
     * Http post.
     *
     * @param path path
     * @param body body
     * @param bodyClazz response body class
     * @return Result
     */
    <T> ResponseResult<T> httpPost(String path, Object body, Class<T> bodyClazz);

    /**
     * Http post.
     *
     * @param path path
     * @param body body
     * @param reference response body class
     * @return Result
     */
    <T> ResponseResult<T> httpPost(String path, Object body, TypeReference<ResponseResult<T>> reference);

    /**
     * Http post.
     *
     * @param path path
     * @param body body
     * @return Result
     */
    ResponseResult<?> httpPost(String path, Object body);

    /**
     * Send HTTP post request by discovery.
     * @param path path
     * @param body body
     * @return response
     */
    ResponseResult<?> httpPostByDiscovery(String path, Object body);

    /**
     * Send HTTP post request by discovery.
     * @param path path
     * @param body body
     * @param bodyClazz response body clazz
     * @return response result
     */
    <T>ResponseResult<T> httpPostByDiscovery(String path, Object body,Class<T> bodyClazz);

    /**
     * Send HTTP get request by dynamic config.
     * @param path path
     * @param headers headers
     * @param paramValues param values
     * @param readTimeoutMs read time out
     * @return result
     */
    ResponseResult<?> httpGetByConfig(String path, Map<String, String> headers, Map<String, String> paramValues,
        long readTimeoutMs);

    /**
     * Send HTTP post request by dynamic config.
     * @param path path
     * @param headers headers
     * @param paramValues param
     * @param readTimeoutMs 超时时间
     * @return result
     */
    ResponseResult<?> httpPostByConfig(String path, Map<String, String> headers, Map<String, String> paramValues,
        long readTimeoutMs);

}

package org.svnee.easyfile.common.request;

import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 自动任务注册请求
 *
 * @author xuzhao
 */
@Data
@Accessors(chain = true)
public class AutoTaskRegisterRequest {

    /**
     * App Id
     */
    private String appId;

    /**
     * 统一标识
     */
    private String unifiedAppId;

    /**
     * key: downloadCode, value: downloadDesc;
     */
    private Map<String, String> downloadCodeMap;
}

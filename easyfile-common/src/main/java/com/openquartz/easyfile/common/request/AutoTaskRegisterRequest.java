package com.openquartz.easyfile.common.request;

import com.openquartz.easyfile.common.dictionary.FileHandleTypeEnum;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 自动任务注册请求
 *
 * @author svnee
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

    /**
     * 文件处理类型
     * @see FileHandleTypeEnum#getCode()
     */
    private Integer handleType;
}

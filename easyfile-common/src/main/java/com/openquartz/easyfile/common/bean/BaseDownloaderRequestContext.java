package com.openquartz.easyfile.common.bean;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * 请求上下文
 *
 * @author svnee
 */
@Data
public class BaseDownloaderRequestContext {

    /**
     * 操作人
     * 必须
     */
    private Notifier notifier;

    /**
     * 文件后缀名
     * 不包含 . 字符
     * 必须
     */
    private String fileSuffix;

    /**
     * 导出备注
     * 非必须
     */
    private String exportRemark;

    /**
     * 非必须
     * 用于参数传递
     */
    private Map<String, Object> otherMap;

    /**
     * 放置参数
     *
     * @param key key
     * @param value value
     */
    public void putParam(String key, Object value) {
        if (otherMap == null) {
            otherMap = new HashMap<>();
        }
        otherMap.put(key, value);
    }

}

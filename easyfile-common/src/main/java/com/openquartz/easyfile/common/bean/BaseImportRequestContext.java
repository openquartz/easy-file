package com.openquartz.easyfile.common.bean;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * 导入请求上下文
 *
 * @author svnee
 */
@Data
public class BaseImportRequestContext {

    /**
     * 操作人
     */
    private Notifier notifier;

    /**
     * 备注
     */
    private String remark;

    /**
     * 参数
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

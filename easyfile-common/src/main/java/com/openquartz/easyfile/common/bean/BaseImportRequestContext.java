package com.openquartz.easyfile.common.bean;

import com.openquartz.easyfile.common.util.MapUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 导入请求上下文
 *
 * @author snvee
 */
@Getter
public class BaseImportRequestContext implements IRequest {

    /**
     * 操作人
     * 必须
     */
    @Setter
    private Notifier notifier;

    /**
     * 导入备注
     */
    @Setter
    private String importRemark;

    /**
     * 非必须
     * 用于参数传递
     */
    private Map<String, Object> otherMap;

    /**
     * 放置参数
     *
     * @param key   key
     * @param value value
     */
    public void putParam(String key, Object value) {
        if (otherMap == null) {
            otherMap = new HashMap<>();
        }
        otherMap.put(key, value);
    }

    public void putParam(Map<String, Object> targetMap) {

        if (MapUtils.isEmpty(targetMap)) {
            return;
        }

        if (Objects.isNull(otherMap)) {
            otherMap = new HashMap<>();
        }
        otherMap.putAll(targetMap);
    }

}

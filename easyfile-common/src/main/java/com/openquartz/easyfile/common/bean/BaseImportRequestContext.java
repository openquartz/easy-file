package com.openquartz.easyfile.common.bean;

import com.openquartz.easyfile.common.util.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 导入请求上下文
 *
 * @author snvee
 */
public class BaseImportRequestContext {

    /**
     * 操作人
     * 必须
     */
    private Notifier notifier;

    /**
     * 导入备注
     */
    private String importRemark;

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

    public void putParam(Map<String,Object> targetMap) {

        if (MapUtils.isEmpty(targetMap)){
            return;
        }

        if(Objects.isNull(otherMap)){
            otherMap = new HashMap<>();
        }
        otherMap.putAll(targetMap);
    }

    public Notifier getNotifier() {
        return notifier;
    }

    public String getImportRemark() {
        return importRemark;
    }

    public Map<String, Object> getOtherMap() {
        return otherMap;
    }

    public void setNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    public void setImportRemark(String importRemark) {
        this.importRemark = importRemark;
    }
}

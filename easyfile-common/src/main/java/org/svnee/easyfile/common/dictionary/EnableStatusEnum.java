package org.svnee.easyfile.common.dictionary;

/**
 * 启用/禁用状态
 *
 * @author svnee
 **/
public enum EnableStatusEnum {

    ENABLE(0, "启用"),
    DISABLE(1, "禁用");
    private final Integer code;
    private final String desc;

    EnableStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

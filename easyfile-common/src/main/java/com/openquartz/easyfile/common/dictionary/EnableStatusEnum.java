package com.openquartz.easyfile.common.dictionary;

/**
 * 启用/禁用状态
 *
 * @author svnee
 **/
public enum EnableStatusEnum implements BaseEnum<Integer> {

    ENABLE(0, "启用"),
    DISABLE(1, "禁用");

    private final Integer code;
    private final String desc;

    EnableStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}

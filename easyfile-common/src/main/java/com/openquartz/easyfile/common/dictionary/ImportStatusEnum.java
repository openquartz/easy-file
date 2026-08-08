package com.openquartz.easyfile.common.dictionary;

import java.util.stream.Stream;

/**
 * 导入状态
 *
 * @author svnee
 */
public enum ImportStatusEnum implements BaseEnum<String> {

    /**
     * 未执行
     */
    NONE("none", "待执行"),

    /**
     * 执行中
     */
    EXECUTING("executing", "执行中"),

    /**
     * 成功
     */
    SUCCESS("success", "成功"),

    /**
     * 部分成功
     */
    PARTIAL_SUCCESS("partial_success", "部分成功"),

    /**
     * 失败
     */
    FAIL("fail", "失败"),

    /**
     * 撤销任务
     */
    CANCEL("revoke", "撤销"),

    /**
     * 过期失效
     */
    INVALID("invalid", "失效"),

    ;

    private final String code;

    private final String desc;

    ImportStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ImportStatusEnum fromCode(String importStatus) {
        return Stream.of(values())
            .filter(importStatusEnum -> importStatusEnum.code.equals(importStatus)).findAny()
            .orElse(null);
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}

package com.openquartz.easyfile.common.dictionary;

import java.util.stream.Stream;

/**
 * 上传状态
 *
 * @author svnee
 */
public enum UploadStatusEnum implements BaseEnum<String> {

    /**
     * 未上传
     */
    NONE("none", "待执行"),

    /**
     * 执行中
     */
    EXECUTING("executing", "执行中"),

    /**
     * 上传中
     */
    UPLOADING("uploading", "上传中"),

    /**
     * 上传成功成功
     */
    SUCCESS("success", "成功"),

    /**
     * 上传失败
     */
    FAIL("fail", "失败"),

    /**
     * 过期失效
     */
    INVALID("invalid", "失效"),

    /**
     * 撤销任务
     */
    CANCEL("revoke", "撤销"),

    ;

    private final String code;

    private final String desc;

    UploadStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UploadStatusEnum fromCode(String uploadStatus) {
        return Stream.of(values())
            .filter(uploadStatusEnum -> uploadStatusEnum.code.equals(uploadStatus)).findAny()
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

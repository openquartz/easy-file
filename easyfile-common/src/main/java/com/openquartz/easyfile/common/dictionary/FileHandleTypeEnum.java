package com.openquartz.easyfile.common.dictionary;

import java.util.stream.Stream;

/**
 * 文件处理类型
 *
 * @author svnee
 */
public enum FileHandleTypeEnum implements BaseEnum<Integer> {

    /**
     * 导入
     */
    IMPORT(0, "导入"),

    /**
     * 导出
     */
    EXPORT(1, "导出"),
    ;

    private final Integer code;
    private final String desc;

    FileHandleTypeEnum(Integer code, String desc) {
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

    public static FileHandleTypeEnum fromCode(Integer handleType) {
        return Stream.of(values())
            .filter(handleTypeEnum -> handleTypeEnum.code.equals(handleType)).findAny()
            .orElse(null);
    }
}

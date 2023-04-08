package com.openquartz.easyfile.example.upload.model;

/**
 * 文件存储服务key
 * @author svnee
 */
public enum FileStorageKeyEnum {
    LOCAL("LOCAL"),
    MINIO("MINIO"),
    ;

    private final String code;

    FileStorageKeyEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

package com.openquartz.easyfile.example.upload.model;

import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class MinioFileIdentifyKey {

    private String bucket;
    private String fileName;

    public MinioFileIdentifyKey() {
    }

    public MinioFileIdentifyKey(String bucket, String fileName) {
        this.bucket = bucket;
        this.fileName = fileName;
    }
}

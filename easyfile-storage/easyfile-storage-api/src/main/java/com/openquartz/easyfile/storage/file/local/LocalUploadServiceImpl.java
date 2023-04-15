package com.openquartz.easyfile.storage.file.local;

import java.io.File;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.storage.file.UploadService;

/**
 * @author svnee
 **/
public class LocalUploadServiceImpl implements UploadService {

    /**
     * 本地存储标识
     */
    public static final String LOCAL = "local";

    @Override
    public Pair<String, String> upload(File file, String fileName, String appId) {
        return Pair.of(LOCAL, file.getAbsolutePath());
    }
}

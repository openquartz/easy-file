package com.openquartz.easyfile.storage.expand;

import com.openquartz.easyfile.storage.exception.FileTransformerErrorCode;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.file.FileUrlTransformer;

/**
 * FileUrlTransformer支持
 *
 * @author svnee
 **/
public class FileUrlTransformerSupport {

    private FileUrlTransformerSupport() {
    }

    private static final Map<String, FileUrlTransformer> TRANSFORMER_MAP = new ConcurrentHashMap<>();

    public static void register(FileUrlTransformer transformer) {
        FileUrlTransformer absentTransformer = TRANSFORMER_MAP.putIfAbsent(transformer.fileSystem(), transformer);
        Asserts.isTrue(Objects.isNull(absentTransformer)
                || absentTransformer.getClass().equals(transformer.getClass()),
            FileTransformerErrorCode.FILE_TRANSFORM_REPEAT_ERROR, transformer.fileSystem());
    }

    public static FileUrlTransformer get(String fileSystem) {
        return TRANSFORMER_MAP.get(fileSystem);
    }

}

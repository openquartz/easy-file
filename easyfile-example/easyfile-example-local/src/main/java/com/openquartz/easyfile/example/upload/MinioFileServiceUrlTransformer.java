package com.openquartz.easyfile.example.upload;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.openquartz.easyfile.example.exception.UploadFileErrorCode;
import com.openquartz.easyfile.example.upload.model.FileStorageKeyEnum;
import com.openquartz.easyfile.example.upload.model.MinioFileIdentifyKey;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.file.FileUrlTransformer;
import com.openquartz.easyfile.common.util.ExceptionUtils;
import com.openquartz.easyfile.common.util.JSONUtil;
import com.openquartz.easyfile.common.util.json.TypeReference;

/**
 * @author svnee
 **/
@Slf4j
public class MinioFileServiceUrlTransformer implements FileUrlTransformer {

    private final MinioClient minioClient;

    private final LoadingCache<MinioFileIdentifyKey, String> locCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(2, TimeUnit.HOURS)
        .concurrencyLevel(4)
        .build(new CacheLoader<MinioFileIdentifyKey, String>() {
            @Override
            public String load(MinioFileIdentifyKey identifyKey) {
                Asserts.notNull(identifyKey, UploadFileErrorCode.FILE_IDENTIFY_KEY_IS_NOT_EXIST);
                try {
                    GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(identifyKey.getBucket())
                        .object(identifyKey.getFileName())
                        .expiry(2, TimeUnit.HOURS)
                        .build();
                    return minioClient.getPresignedObjectUrl(args);
                } catch (Exception ex) {
                    return ExceptionUtils.rethrow(ex);
                }
            }
        });

    public MinioFileServiceUrlTransformer(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String fileSystem() {
        return FileStorageKeyEnum.MINIO.getCode();
    }

    @Override
    public String transform(String fileKey) {
        MinioFileIdentifyKey identifyKey = JSONUtil.parseObject(fileKey, new TypeReference<MinioFileIdentifyKey>() {
        });
        Asserts.notNull(identifyKey, UploadFileErrorCode.FILE_IDENTIFY_KEY_IS_NOT_EXIST);
        try {
            return locCache.get(identifyKey);
        } catch (Exception ex) {
            return ExceptionUtils.rethrow(ex);
        }
    }
}

package com.openquartz.easyfile.example.upload;

import com.openquartz.easyfile.example.config.MinioProperties;
import com.openquartz.easyfile.example.upload.model.FileStorageKeyEnum;
import com.openquartz.easyfile.example.upload.model.MinioFileIdentifyKey;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.util.ExceptionUtils;
import com.openquartz.easyfile.common.util.JSONUtil;
import com.openquartz.easyfile.storage.file.UploadService;

/**
 * @author svnee
 **/
@Slf4j
public class MinioUploadServiceImpl implements UploadService {

    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    public MinioUploadServiceImpl(MinioProperties minioProperties, MinioClient minioClient) {
        this.minioProperties = minioProperties;
        this.minioClient = minioClient;
    }

    @Override
    public Pair<String, String> upload(File file, String fileName, String appId) {

        try (final InputStream is = new FileInputStream(file)) {

            PutObjectArgs build = PutObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(fileName)
                .stream(is, is.available(), -1)
                .build();
            minioClient.putObject(build);
            MinioFileIdentifyKey entry = new MinioFileIdentifyKey(minioProperties.getBucket(), fileName);

            return Pair.of(FileStorageKeyEnum.MINIO.getCode(), JSONUtil.toJson(entry));
        } catch (Exception ex){
            log.error("[MinioUploadServiceImpl#upload] file-name:{},appId:{},upload error!",fileName,appId,ex);
            return ExceptionUtils.rethrow(ex);
        }
    }
}

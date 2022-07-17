package org.svnee.easyfile.example.upload;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * @author svnee
 **/
@Slf4j
public class ActualUploadServiceImpl implements UploadService {

    @Override
    public Pair<String, String> upload(File file, String fileName, String appId) {
        log.info("[ActualUploadService#upload] file:{},fileName:{},appId:{}", file.getName(), fileName, appId);
        return Pair.of("LOCAL", file.getAbsolutePath());
    }


}

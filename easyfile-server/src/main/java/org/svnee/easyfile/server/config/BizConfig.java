package org.svnee.easyfile.server.config;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.common.util.TypeReference;

/**
 * 业务配置config
 *
 * @author svnee
 **/
@Configuration
public class BizConfig {

    @Value("${easyfile.server.file.invalid.time:{}}")
    private String fileInvalidTimeMap;

    /**
     * 查询文件失效时间
     *
     * @return 失效时间
     */
    public Map<String, Integer> getFileInvalidTimeMap() {
        return JSONUtil.parseObject(fileInvalidTimeMap, new TypeReference<Map<String, Integer>>() {
        });
    }
}

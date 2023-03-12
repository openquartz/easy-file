package org.svnee.easyfile.server.config;

import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.common.util.json.TypeReference;

/**
 * 业务配置config
 *
 * @author svnee
 **/
@Data
@Configuration
public class BizConfig {

    @Value("${easyfile.server.file.invalid.time:{}}")
    private String fileInvalidTimeMap;

    @Value("${easyfile.server.email.from:}")
    private String emailFrom;

    @Value("${easyfile.server.email.admin:}")
    private String emailAdminName;

    @Value("${spring.application.name}")
    private String appId;

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

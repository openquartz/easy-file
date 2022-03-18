package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author svnee
 * @desc 下载参数
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = EasyFileDownloadProperties.PREFIX)
public class EasyFileDownloadProperties {

    public static final String PREFIX = "easyfile.download";

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 服务APPID
     */
    private String appId;

    /**
     * 服务统一标识
     */
    private String unifiedAppId;

    /**
     * 本地文件临时存放地址
     * 默认：/tmp
     */
    private String localFileTempPath = "/tmp";

    /**
     * 是否开启自动注册
     * 默认为 false
     */
    private boolean enableAutoRegister = false;

    /**
     * 是否开启文件压缩优化
     */
    private boolean enableCompressFile = false;

    /**
     * 启用文件压缩最小的大小，单位:MB
     * 在启用文件压缩后生效
     */
    private Integer minEnableCompressMbSize = 1;

    /**
     * 切面顺序,默认 Integer.MAX_VALUE
     */
    private Integer exportAdvisorOrder = Integer.MAX_VALUE;

}

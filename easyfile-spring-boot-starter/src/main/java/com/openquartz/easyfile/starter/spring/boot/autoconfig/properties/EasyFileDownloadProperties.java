package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.openquartz.easyfile.common.bean.excel.ExcelGenProperty;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.core.property.IEasyFileDownloadProperty;

/**
 * 下载配置参数
 *
 * @author svnee
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = EasyFileDownloadProperties.PREFIX)
public class EasyFileDownloadProperties implements IEasyFileDownloadProperty {

    public static final String PREFIX = "easyfile.download";

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 服务APPID
     * app-id
     */
    private String appId;

    /**
     * 服务统一标识
     * unified-app-id 默认使用{@link #appId}
     */
    private String unifiedAppId;

    /**
     * 本地文件临时存放地址
     * 默认：/tmp
     * local-file-temp-path=/temp
     */
    private String localFileTempPath = "/temp";

    /**
     * 是否开启自动注册
     * 默认为 false
     * enable-auto-register=false
     */
    private boolean enableAutoRegister = false;

    /**
     * 是否开启文件压缩优化
     * enable-compress-file=false
     */
    private boolean enableCompressFile = false;

    /**
     * 启用文件压缩最小的大小，单位:MB
     * 在启用文件压缩后生效
     * min-enable-compress-mb-size=1
     */
    private int minEnableCompressMbSize = 1;

    /**
     * 切面顺序,默认 Integer.MAX_VALUE
     * export-advisor-order
     */
    private Integer exportAdvisorOrder = Integer.MAX_VALUE;

    /**
     * excel 最大Sheet行数
     * excel-max-sheet-rows
     */
    private Integer excelMaxSheetRows = 1000000;

    /**
     * excel 缓存到内存行数-刷盘
     * excel-row-access-window-size
     */
    private Integer excelRowAccessWindowSize = 1000;

    /**
     * 上传完成后删除文件
     * clean-file-after-upload
     * 默认为：true. 建议设置成true。本地测试时不需要上传到远程服务时可以为false
     */
    private boolean cleanFileAfterUpload = true;

    /**
     * EasyFile 触发类型
     * 支持：default,schedule,rocketmq,disruptor
     */
    private DownloadAsyncTriggerType asyncTriggerType = DownloadAsyncTriggerType.DEFAULT;

    @Override
    public String getUnifiedAppId() {
        return StringUtils.isBlank(unifiedAppId) ? appId : unifiedAppId;
    }

    public void setExcelMaxSheetRows(Integer excelMaxSheetRows) {
        ExcelGenProperty.setSegmentationSheetRows(excelMaxSheetRows);
        this.excelMaxSheetRows = excelMaxSheetRows;
    }

    public void setExcelRowAccessWindowSize(Integer excelRowAccessWindowSize) {
        ExcelGenProperty.setRowAccessWindowSize(excelRowAccessWindowSize);
        this.excelRowAccessWindowSize = excelRowAccessWindowSize;
    }
}

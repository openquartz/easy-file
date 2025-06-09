package com.openquartz.easyfile.common.request;

import com.openquartz.easyfile.common.bean.BaseExecuteParam;
import com.openquartz.easyfile.common.bean.Notifier;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 注册下载请求
 *
 * @author svnee
 */
public class RegisterDownloadRequest extends BaseExecuteParam {

    /**
     * 服务名
     * 必填
     */
    @NotBlank(message = "appId not blank")
    private String appId;

    /**
     * 是否开启下载完成通知
     * 必填
     */
    @NotNull(message = "是否开启同步不能为空")
    private Boolean enableNotify;

    /**
     * 通知人
     * 必填
     */
    @NotNull(message = "通知人信息不能为空")
    private Notifier notifier;

    /**
     * 导出备注
     * 非必须
     */
    private String exportRemark;

    /**
     * 当前服务最大的重试次数
     */
    private Integer maxServerRetry;

    /**
     * locale
     */
    private String locale;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Boolean getEnableNotify() {
        return enableNotify;
    }

    public void setEnableNotify(Boolean enableNotify) {
        this.enableNotify = enableNotify;
    }

    public Notifier getNotifier() {
        return notifier;
    }

    public void setNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    public String getExportRemark() {
        return exportRemark;
    }

    public void setExportRemark(String exportRemark) {
        this.exportRemark = exportRemark;
    }

    public Integer getMaxServerRetry() {
        return maxServerRetry;
    }

    public void setMaxServerRetry(Integer maxServerRetry) {
        this.maxServerRetry = maxServerRetry;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}

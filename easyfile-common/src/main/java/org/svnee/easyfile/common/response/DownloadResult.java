package org.svnee.easyfile.common.response;

import java.util.Date;

/**
 * 下载结果
 *
 * @author svnee
 */
public class DownloadResult extends ExportResult {

    /**
     * 下载码
     */
    private String downloadCode;

    /**
     * 下载Desc
     */
    private String downloadCodeDesc;

    /**
     * 下载操作人
     */
    private String downloadOperateBy;

    /**
     * 下载操作人
     */
    private String downloadOperateName;

    /**
     * 下载时间
     */
    private Date exportTime;

    /**
     * 最新执行时间
     */
    private Date lastExecuteTime;

    /**
     * 失败信息
     */
    private String errorMsg;

    /**
     * 失效时间
     */
    private Date invalidTime;

    /**
     * 下载次数
     */
    private Integer downloadNum;

    /**
     * 更新人
     */
    private String updateBy;

    public String getDownloadCode() {
        return downloadCode;
    }

    public void setDownloadCode(String downloadCode) {
        this.downloadCode = downloadCode;
    }

    public String getDownloadCodeDesc() {
        return downloadCodeDesc;
    }

    public void setDownloadCodeDesc(String downloadCodeDesc) {
        this.downloadCodeDesc = downloadCodeDesc;
    }

    public String getDownloadOperateBy() {
        return downloadOperateBy;
    }

    public void setDownloadOperateBy(String downloadOperateBy) {
        this.downloadOperateBy = downloadOperateBy;
    }

    public String getDownloadOperateName() {
        return downloadOperateName;
    }

    public void setDownloadOperateName(String downloadOperateName) {
        this.downloadOperateName = downloadOperateName;
    }

    public Date getExportTime() {
        return exportTime;
    }

    public void setExportTime(Date exportTime) {
        this.exportTime = exportTime;
    }

    public Date getLastExecuteTime() {
        return lastExecuteTime;
    }

    public void setLastExecuteTime(Date lastExecuteTime) {
        this.lastExecuteTime = lastExecuteTime;
    }

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Date getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Date invalidTime) {
        this.invalidTime = invalidTime;
    }

    public Integer getDownloadNum() {
        return downloadNum;
    }

    public void setDownloadNum(Integer downloadNum) {
        this.downloadNum = downloadNum;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
}

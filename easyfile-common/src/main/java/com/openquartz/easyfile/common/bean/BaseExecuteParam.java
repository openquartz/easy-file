package com.openquartz.easyfile.common.bean;

import com.openquartz.easyfile.common.dictionary.FileSuffixEnum;
import java.util.Map;
import javax.validation.constraints.NotBlank;

/**
 * 执行参数
 *
 * @author svnee
 */
public class BaseExecuteParam {

    /**
     * 下载code
     * 必填
     */
    @NotBlank(message = "下载编码不能为空")
    private String downloadCode;

    /**
     * 文件后缀名
     * 必填
     *
     * @see FileSuffixEnum#getCode()
     */
    @NotBlank(message = "文件后缀名不能为空")
    private String fileSuffix;

    /**
     * 其他参数
     * 非必须
     */
    private Map<String, Object> otherMap;

    public String getDownloadCode() {
        return downloadCode;
    }

    public void setDownloadCode(String downloadCode) {
        this.downloadCode = downloadCode;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public Map<String, Object> getOtherMap() {
        return otherMap;
    }

    public void setOtherMap(Map<String, Object> otherMap) {
        this.otherMap = otherMap;
    }
}

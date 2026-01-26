package com.openquartz.easyfile.common.request;

import com.openquartz.easyfile.common.bean.BaseImportRequestContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 注册导入请求
 *
 * @author svnee
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterImportRequest extends BaseImportRequestContext {

    /**
     * App ID
     */
    private String appId;

    /**
     * 导入任务编码
     */
    private String importCode;

    /**
     * 文件地址
     */
    private String fileUrl;
}

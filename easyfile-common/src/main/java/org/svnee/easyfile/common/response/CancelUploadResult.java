package org.svnee.easyfile.common.response;

import lombok.Data;

/**
 * 撤销上传结果
 *
 * @author svnee
 */
@Data
public class CancelUploadResult {

    /**
     * 撤销结果
     */
    private boolean cancelResult;

    /**
     * 撤销消息
     */
    private String cancelMsg;

}

package com.openquartz.easyfile.common.response;

import lombok.Data;

/**
 * 撤销导入结果
 *
 * @author svnee
 */
@Data
public class CancelImportResult {

    /**
     * 撤销结果
     */
    private boolean cancelResult;

    /**
     * 撤销消息
     */
    private String cancelMsg;

}

package com.openquartz.easyfile.common.response;

import lombok.Data;

/**
 * 导入结果
 * @author svnee
 */
@Data
public class ImportResult {

    /**
     * 成功导入行数
     */
    private Integer successImportRows;

    /**
     * 失败导入行数
     */
    private Integer failedImportRows;

}

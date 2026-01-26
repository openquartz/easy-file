package com.openquartz.easyfile.common.bean;

import lombok.Data;

/**
 * Import Record Info
 *
 * @author svnee
 */
@Data
public class ImportRecordInfo {
    
    private Long id;
    private String appId;
    private String importCode;
    private String fileUrl;
    private String fileName;
}

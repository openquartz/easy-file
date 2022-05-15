package org.svnee.easyfile.example.entity.response;

import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class ExportResultVO {

    private Long downloadTaskId;

    private String downloadMsg;

    public ExportResultVO() {
    }

    public ExportResultVO(Long downloadTaskId, String downloadMsg) {
        this.downloadTaskId = downloadTaskId;
        this.downloadMsg = downloadMsg;
    }
}

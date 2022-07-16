package org.svnee.easyfile.common.response;

import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class DownloadTriggerResult {

    private Long registerId;

    private Integer triggerCount;

}

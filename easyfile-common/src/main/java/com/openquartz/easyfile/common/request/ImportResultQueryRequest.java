package com.openquartz.easyfile.common.request;

import com.openquartz.easyfile.common.util.page.Page;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

/**
 * 导入结果查询请求
 *
 * @author svnee
 */
@Data
public class ImportResultQueryRequest extends Page {

    private String appId;

    private String importCode;

    private String importStatus;

    private Date startTime;

    private Date endTime;

    private String createBy;

    private String businessUniqueKey;

    public ImportResultQueryRequest() {
        super(1, 20, 0);
    }

    @Builder
    public ImportResultQueryRequest(String appId, String importCode, String importStatus,
                                    Date startTime, Date endTime, String createBy,
                                    String businessUniqueKey) {
        super(1, 20, 0);
        this.appId = appId;
        this.importCode = importCode;
        this.importStatus = importStatus;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createBy = createBy;
        this.businessUniqueKey = businessUniqueKey;
    }
}

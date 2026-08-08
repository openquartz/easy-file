package com.openquartz.easyfile.common.response;

import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 导入结果
 *
 * @author svnee
 */
@Data
@Builder
public class ImportResult {

    private Long registerId;

    private boolean success;

    private Integer process;

    private Long totalCount;

    private Long successCount;

    private Long failCount;

    private String resultFileUrl;

    private List<ErrorColumnInfo> errorColumns;

    private String errorMsg;

    /**
     * Source file MD5 (stored with idempotent mark for same-key / different-hash reject).
     */
    private String fileHash;

    private String status;

    private Date startTime;

    private Date endTime;

    private Long duration;

    private Boolean isIdempotent = false;
}

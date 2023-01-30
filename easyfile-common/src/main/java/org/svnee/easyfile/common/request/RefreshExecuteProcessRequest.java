package org.svnee.easyfile.common.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.serdes.BaseEnumDeserializer;
import org.svnee.easyfile.common.serdes.BaseEnumSerializer;

/**
 * 刷新执行进度请求
 *
 * @author svnee
 **/
@Data
public class RefreshExecuteProcessRequest {

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 执行进度
     */
    private Integer executeProcess;

    /**
     * 下一个上传状态
     */
    @JsonSerialize(using = BaseEnumSerializer.class)
    @JsonDeserialize(using = BaseEnumDeserializer.class)
    private UploadStatusEnum nextUploadStatus;

}

package org.svnee.easyfile.storage.mapper.condition;

import lombok.Data;

import java.util.Date;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;

/**
 * 上传成功信息条件
 *
 * @author xuzhao
 */
@Data
public class UploadInfoChangeCondition {

    private Long id;

    /**
     * 上传状态
     */
    private UploadStatusEnum uploadStatus;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 文件所在系统
     */
    private String fileSystem;

    /**
     * 异常信息
     */
    private String errorMsg;

    /**
     * 最新执行时间
     */
    private Date lastExecuteTime;

    /**
     * 失效时间
     */
    private Date invalidTime;

}

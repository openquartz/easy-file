package com.openquartz.easyfile.storage.dictionary;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.openquartz.easyfile.common.dictionary.BaseEnum;
import com.openquartz.easyfile.common.util.CollectionUtils;

/**
 * 异步下载触发器执行状态
 *
 * @author svnee
 **/
@Getter
@AllArgsConstructor
public enum DownloadTriggerStatusEnum implements BaseEnum<String> {

    /**
     * 初始化
     */
    INIT("init", "初始化"),

    /**
     * 排队中
     */
    WAITING("waiting", "排队中"),

    /**
     * 执行中
     */
    EXECUTING("executing", "执行中"),

    /**
     * 成功
     */
    SUCCESS("success", "成功"),

    /**
     * 失败
     */
    FAIL("fail", "失败"),

    ;

    /**
     * code
     */
    private final String code;

    /**
     * desc
     */
    private final String desc;

    public static final List<DownloadTriggerStatusEnum> EXE_TRIGGER_STATUS_LIST = CollectionUtils
        .newArrayList(INIT, FAIL);

    public static DownloadTriggerStatusEnum ofCode(String triggerStatus) {
        for (DownloadTriggerStatusEnum value : values()) {
            if (value.code.equals(triggerStatus)) {
                return value;
            }
        }
        return null;
    }
}

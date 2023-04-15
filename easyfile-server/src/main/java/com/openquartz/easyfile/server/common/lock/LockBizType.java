package com.openquartz.easyfile.server.common.lock;

import com.openquartz.easyfile.common.dictionary.BaseEnum;

/**
 * LockBizType
 *
 * @author svnee
 */
public enum LockBizType implements BaseEnum<String> {

    /**
     * 下载运行锁
     * 0: 下载记录注册ID
     */
    DOWNLOAD_RUNNING_LOCK("download_running", "下载运行锁"),

    /**
     * 下载任务自动注册锁
     * 0：服务appId
     */
    TASK_AUTO_REGISTER_LOCK("task_auto_register", "下载任务自动注册锁"),
    ;

    private final String code;
    private final String desc;

    LockBizType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}

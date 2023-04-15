package com.openquartz.easyfile.server.notify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.openquartz.easyfile.common.dictionary.BaseEnum;

/**
 * 通知策略
 *
 * @author svnee
 */
@Getter
@AllArgsConstructor
public enum NotifyWay implements BaseEnum<String> {

    /**
     * Wechat推送
     */
    WECHAT("wechat", "微信机器人通知"),
    ;
    private final String code;
    private final String desc;


    public static NotifyWay of(String code) {
        for (NotifyWay value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}

package com.openquartz.easyfile.server.service.executor;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 限流常量
 *
 * @author svnee
 */
@AllArgsConstructor
@Getter
public enum LimitingStrategy {

    NONE(LimitingConstants.NONE, "不使用限流"),
    GLOBAL_APP(LimitingConstants.GLOBAL_APP, "APP维度限流"),
    ;

    private final String code;

    private final String desc;


}

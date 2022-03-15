package org.svnee.easyfile.common.bean;

import lombok.Data;

/**
 * 通知信息
 *
 * @author xuzhao
 */
@Data
public class Notifier {

    /**
     * 用户信息
     */
    private String userBy;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 邮箱
     */
    private String email;

}

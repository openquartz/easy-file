package org.svnee.easyfile.server.service;

import org.svnee.easyfile.server.entity.UserInfo;

/**
 * User service.
 *
 * @author svnee
 */
public interface UserService {

    /**
     * 根据username 查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfo getByUsername(String username);

}

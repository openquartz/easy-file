package org.svnee.easyfile.server.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.svnee.easyfile.server.entity.UserInfo;
import org.svnee.easyfile.server.mapper.UserInfoMapper;
import org.svnee.easyfile.server.service.UserService;

/**
 * User service impl.
 *
 * @author svnee
 * @date 2021/10/30 21:40
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoMapper userInfoMapper;

    @Override
    public UserInfo getByUsername(String username) {
        return userInfoMapper.getByUsername(username);
    }
}

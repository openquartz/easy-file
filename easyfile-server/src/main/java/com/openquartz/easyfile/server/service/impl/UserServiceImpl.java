package com.openquartz.easyfile.server.service.impl;

import com.openquartz.easyfile.server.entity.UserInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.openquartz.easyfile.server.mapper.UserInfoMapper;
import com.openquartz.easyfile.server.service.UserService;

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

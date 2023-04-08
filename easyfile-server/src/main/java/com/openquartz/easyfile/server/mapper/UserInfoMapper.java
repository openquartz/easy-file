package com.openquartz.easyfile.server.mapper;

import com.openquartz.easyfile.server.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * User mapper.
 *
 * @author svnee
 * @date 2021/10/30 21:42
 */
@Mapper
public interface UserInfoMapper {

    /**
     * insert selective
     *
     * @param userInfo userInfo
     * @return affect
     */
    int insertSelective(UserInfo userInfo);

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfo getByUsername(@Param("username") String username);
}

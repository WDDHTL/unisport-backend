package com.unisport.service;

import com.unisport.vo.UserProfileVO;

/**
 * 用户领域服务接口。
 */
public interface UserService {

    /**
     * 根据用户ID获取用户主页信息。
     *
     * @param userId 目标用户ID
     * @return 用户主页视图对象
     */
    UserProfileVO getUserProfile(Long userId);
}

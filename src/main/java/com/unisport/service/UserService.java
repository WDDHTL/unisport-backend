package com.unisport.service;

import com.unisport.common.PageResult;
import com.unisport.dto.UpdateUserDTO;
import com.unisport.vo.FollowUserVO;
import com.unisport.vo.UserProfileVO;

/**
 * User domain service interface.
 */
public interface UserService {

    /**
     * Get user profile by user id.
     *
     * @param userId target user id
     * @return profile view object
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * Update current user's profile.
     *
     * @param userId        user id from path
     * @param updateUserDTO update payload
     * @return updated profile view
     */
    UserProfileVO updateUserProfile(Long userId, UpdateUserDTO updateUserDTO);

    /**
     * 关注指定用户。
     *
     * @param targetUserId 被关注用户ID
     */
    void followUser(Long targetUserId);

    /**
     * 取消关注指定用户。
     *
     * @param targetUserId 取消关注的用户ID
     */
    void unfollowUser(Long targetUserId);

    /**
     * 查询某个用户的关注列表（分页）。
     *
     * @param userId  关注人用户ID
     * @param current 当前页码
     * @param size    每页数量
     * @return 分页的关注用户列表
     */
    PageResult<FollowUserVO> getFollowingList(Long userId, long current, long size);
}

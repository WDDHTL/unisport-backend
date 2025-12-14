package com.unisport.service;

import com.unisport.dto.UpdateUserDTO;
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
}

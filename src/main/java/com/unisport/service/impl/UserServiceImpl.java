package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unisport.common.BusinessException;
import com.unisport.common.PageResult;
import com.unisport.common.UserContext;
import com.unisport.dto.UpdateUserDTO;
import com.unisport.entity.Post;
import com.unisport.entity.User;
import com.unisport.entity.UserFollow;
import com.unisport.mapper.PostMapper;
import com.unisport.mapper.UserFollowMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.service.UserService;
import com.unisport.vo.FollowUserVO;
import com.unisport.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户领域服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserFollowMapper userFollowMapper;
    private final PostMapper postMapper;

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        log.info("查询用户主页信息，目标用户ID={}", userId);

        User targetUser = userMapper.selectById(userId);
        if (targetUser == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        Long followersCount = userFollowMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowingId, userId)
        );

        Long followingCount = userFollowMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, userId)
        );

        Long postsCount = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getUserId, userId)
        );

        boolean isFollowing = false;
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null && !currentUserId.equals(userId)) {
            Long relationCount = userFollowMapper.selectCount(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getFollowerId, currentUserId)
                            .eq(UserFollow::getFollowingId, userId)
            );
            isFollowing = relationCount != null && relationCount > 0;
        }

        return UserProfileVO.builder()
                .id(targetUser.getId())
                .nickname(targetUser.getNickname())
                .avatar(targetUser.getAvatar())
//                .school(targetUser.getSchool())
                .schoolId(targetUser.getSchoolId())
//                .department(targetUser.getDepartment())
                .bio(targetUser.getBio())
                .gender(targetUser.getGender())
                .followersCount(followersCount)
                .followingCount(followingCount)
                .postsCount(postsCount)
                .following(isFollowing)
                .build();
    }

    @Override
    public UserProfileVO updateUserProfile(Long userId, UpdateUserDTO updateUserDTO) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        if (!currentUserId.equals(userId)) {
            throw new BusinessException(40301, "您没有权限修改该用户信息");
        }

        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        log.info("更新用户基本信息，userId={}", userId);

        User updateEntity = new User();
        updateEntity.setId(userId);

        if (updateUserDTO.getNickname() != null) {
            String nickname = updateUserDTO.getNickname().trim();
            if (nickname.isEmpty()) {
                throw new BusinessException(40004, "昵称不能为空");
            }
            updateEntity.setNickname(nickname);
        }
        if (updateUserDTO.getAvatar() != null) {
            updateEntity.setAvatar(updateUserDTO.getAvatar().trim());
        }
        if (updateUserDTO.getBio() != null) {
            updateEntity.setBio(updateUserDTO.getBio().trim());
        }
        if (updateUserDTO.getGender() != null) {
            Integer gender = updateUserDTO.getGender();
            if (gender != 0 && gender != 1) {
                throw new BusinessException(40004, "性别取值仅支持0（女）或1（男）");
            }
            updateEntity.setGender(gender);
        }

        boolean hasChanges = updateEntity.getNickname() != null
                || updateEntity.getAvatar() != null
                || updateEntity.getBio() != null
                || updateEntity.getGender() != null;
        if (!hasChanges) {
            return getUserProfile(userId);
        }

        int rows = userMapper.updateById(updateEntity);
        if (rows <= 0) {
            throw new BusinessException(50001, "更新失败，请稍后重试");
        }

        return getUserProfile(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followUser(Long targetUserId) {
        Long currentUserId = requireLogin();
        validateTargetUser(targetUserId, currentUserId);

        Long exists = userFollowMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, currentUserId)
                        .eq(UserFollow::getFollowingId, targetUserId)
        );
        if (exists != null && exists > 0) {
            throw new BusinessException(40901, "请勿重复关注");
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(currentUserId);
        follow.setFollowingId(targetUserId);

        try {
            int rows = userFollowMapper.insert(follow);
            if (rows <= 0) {
                throw new BusinessException(50001, "关注失败，请稍后重试");
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException(40901, "请勿重复关注");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowUser(Long targetUserId) {
        Long currentUserId = requireLogin();
        validateTargetUser(targetUserId, currentUserId);

        int rows = userFollowMapper.delete(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, currentUserId)
                        .eq(UserFollow::getFollowingId, targetUserId)
        );
        if (rows <= 0) {
            throw new BusinessException(40901, "尚未关注该用户");
        }
    }

    @Override
    public PageResult<FollowUserVO> getFollowingList(Long userId, long current, long size) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        long pageNum = current <= 0 ? 1 : current;
        long pageSize = size <= 0 ? 20 : size;

        Page<UserFollow> page = userFollowMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, userId)
                        .orderByDesc(UserFollow::getCreatedAt)
        );

        List<Long> followingIds = page.getRecords().stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toList());

        Map<Long, User> resolvedUserMap;
        if (!CollectionUtils.isEmpty(followingIds)) {
            List<User> users = userMapper.selectBatchIds(followingIds);
            resolvedUserMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        } else {
            resolvedUserMap = Collections.emptyMap();
        }

        Long currentUserId = UserContext.getUserId();
        Set<Long> resolvedFollowings;
        if (!CollectionUtils.isEmpty(followingIds) && currentUserId != null) {
            if (currentUserId.equals(userId)) {
                resolvedFollowings = followingIds.stream().collect(Collectors.toSet());
            } else {
                List<UserFollow> relations = userFollowMapper.selectList(
                        new LambdaQueryWrapper<UserFollow>()
                                .eq(UserFollow::getFollowerId, currentUserId)
                                .in(UserFollow::getFollowingId, followingIds)
                );
                resolvedFollowings = relations.stream()
                        .map(UserFollow::getFollowingId)
                        .collect(Collectors.toSet());
            }
        } else {
            resolvedFollowings = Collections.emptySet();
        }

        List<FollowUserVO> records = page.getRecords().stream()
                .map(relation -> {
                    User followee = resolvedUserMap.get(relation.getFollowingId());
                    if (followee == null) {
                        return null;
                    }
                    FollowUserVO vo = new FollowUserVO();
                    vo.setId(followee.getId());
                    vo.setNickname(followee.getNickname());
                    vo.setAvatar(followee.getAvatar());
                    vo.setSchoolId(followee.getSchoolId());
//                    vo.setSchool(followee.getSchool());
//                    vo.setDepartment(followee.getDepartment());
                    vo.setBio(followee.getBio());
                    vo.setGender(followee.getGender());
                    vo.setFollowing(resolvedFollowings.contains(followee.getId()));
                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PageResult.of(pageNum, pageSize, page.getTotal(), page.getPages(), records);
    }

    private void validateTargetUser(Long targetUserId, Long currentUserId) {
        if (targetUserId == null) {
            throw new BusinessException(40004, "目标用户ID不能为空");
        }
        if (targetUserId.equals(currentUserId)) {
            throw new BusinessException(40004, "不能关注或取消关注自己");
        }
        if (userMapper.selectById(targetUserId) == null) {
            throw new BusinessException(40401, "用户不存在");
        }
    }

    private Long requireLogin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        return userId;
    }
}

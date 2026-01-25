package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unisport.Enum.NotifyType;
import com.unisport.common.BusinessException;
import com.unisport.common.UserContext;
import com.unisport.dto.NotificationQueryDTO;
import com.unisport.entity.Notification;
import com.unisport.entity.User;
import com.unisport.mapper.NotificationMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.service.NotificationService;
import com.unisport.vo.NotificationListVO;
import com.unisport.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Notification domain service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @Override
    public NotificationListVO listNotifications(NotificationQueryDTO queryDTO) {
        Long userId = requireLogin();
        NotifyType notifyType = resolveNotifyType(queryDTO.getType());
        long pageNum = queryDTO.getCurrent() == null || queryDTO.getCurrent() <= 0 ? 1L : queryDTO.getCurrent();
        long pageSize = queryDTO.getSize() == null || queryDTO.getSize() <= 0 ? 20L : queryDTO.getSize();

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (notifyType != null) {
            wrapper.eq(Notification::getType, notifyType);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);

        Page<Notification> page = notificationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Map<Long, User> senderMap = resolveSenders(page.getRecords());
        List<NotificationVO> records = page.getRecords().stream()
                .map(notification -> buildNotificationVO(notification, senderMap))
                .collect(Collectors.toList());

        long unreadCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
        );

        NotificationListVO vo = new NotificationListVO();
        vo.setRecords(records);
        vo.setUnreadCount(unreadCount);
        vo.setTotal(page.getTotal());
        vo.setCurrent(pageNum);
        vo.setSize(pageSize);
        vo.setPages(page.getPages());
        return vo;
    }

    @Override
    public void markAsRead(Long notificationId) {
        Long userId = requireLogin();
        if (notificationId == null || notificationId <= 0) {
            throw new BusinessException(40004, "通知ID不合法");
        }
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(40401, "通知不存在");
        }
        if (!userId.equals(notification.getUserId())) {
            throw new BusinessException(40301, "您没有权限操作该通知");
        }
        if (notification.getIsRead() == 1) {
            return;
        }

        int rows = notificationMapper.update(
                null,
                new UpdateWrapper<Notification>()
                        .eq("id", notificationId)
                        .eq("user_id", userId)
                        .eq("is_read", 0)
                        .set("is_read", 1)
        );
        if (rows <= 0) {
            throw new BusinessException(50001, "标记已读失败，请稍后重试");
        }
    }

    @Override
    public void markAllAsRead() {
        Long userId = requireLogin();
        Long unreadCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
        );
        if (unreadCount == null || unreadCount == 0) {
            return;
        }
        int rows = notificationMapper.update(
                null,
                new UpdateWrapper<Notification>()
                        .eq("user_id", userId)
                        .eq("is_read", 0)
                        .set("is_read", 1)
        );
        if (rows <= 0) {
            throw new BusinessException(50001, "标记全部已读失败，请稍后重试");
        }
    }

    @Override
    public Long getUnreadCount() {
        Long userId = requireLogin();
        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
        );
        return count == null ? 0L : count;
    }

    private NotifyType resolveNotifyType(String type) {
        if (type == null || "all".equalsIgnoreCase(type.trim())) {
            return null;
        }
        try {
            return NotifyType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(40004, "通知类型不合法，请确认为 like/comment/follow/system/all");
        }
    }

    private Map<Long, User> resolveSenders(List<Notification> notifications) {
        if (CollectionUtils.isEmpty(notifications)) {
            return Collections.emptyMap();
        }
        Set<Long> senderIds = notifications.stream()
                .map(Notification::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(senderIds)) {
            return Collections.emptyMap();
        }
        List<User> users = userMapper.selectBatchIds(senderIds);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyMap();
        }
        return users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private NotificationVO buildNotificationVO(Notification notification, Map<Long, User> senderMap) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType() != null ? notification.getType().getValue() : null);
        User sender = senderMap.get(notification.getSenderId());
        if (sender != null) {
            vo.setUserName(sender.getNickname());
        } else if (notification.getType() == NotifyType.SYSTEM) {
            vo.setUserName("系统通知");
        } else {
            vo.setUserName("未知用户");
        }
        vo.setContent(notification.getContent());
        vo.setRelatedType(notification.getRelatedType() != null ? notification.getRelatedType().getValue() : null);
        vo.setRelatedId(notification.getRelatedId());
        vo.setPostId(notification.getPostId());
        vo.setIsRead(notification.getIsRead() == 1);
        vo.setCreatedAt(notification.getCreatedAt());
        return vo;
    }

    private Long requireLogin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        return userId;
    }
}

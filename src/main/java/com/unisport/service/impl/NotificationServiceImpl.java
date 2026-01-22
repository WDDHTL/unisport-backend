package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
 * 通知模块业务实现。
 * <p>负责根据用户、类型、分页条件查询通知，并返回前端所需的汇总数据。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    /**
     * 获取当前登录用户的通知分页列表，同时附带未读数量。
     *
     * @param queryDTO 查询条件（类型过滤、页码、分页大小）
     * @return 包含列表、未读数与分页元信息的响应对象
     */
    @Override
    public NotificationListVO listNotifications(NotificationQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }

        NotifyType notifyType = resolveNotifyType(queryDTO.getType());
        long pageNum = queryDTO.getCurrent() == null || queryDTO.getCurrent() <= 0 ? 1L : queryDTO.getCurrent();
        long pageSize = queryDTO.getSize() == null || queryDTO.getSize() <= 0 ? 20L : queryDTO.getSize();

        // 构建查询条件：限定当前用户、可选类型过滤，按时间倒序
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (notifyType != null) {
            wrapper.eq(Notification::getType, notifyType);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);

        Page<Notification> page = notificationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 预先批量加载触发人信息，避免 N+1 查询
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

    /**
     * 将前端传入的字符串类型解析为 NotifyType 枚举。
     *
     * @param type 文档约定的类型字符串
     * @return 匹配的枚举，若为 all 或空则返回 null 表示不做类型过滤
     */
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

    /**
     * 批量查询触发通知的用户信息并转为 Map，减少后续查库次数。
     *
     * @param notifications 通知列表
     * @return senderId -> User 的映射表
     */
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

    /**
     * 将通知实体转换为前端所需的 VO。
     *
     * @param notification 通知实体
     * @param senderMap    已查询的发送人信息
     * @return 转换后的 VO
     */
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
}

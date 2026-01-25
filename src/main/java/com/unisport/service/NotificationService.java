package com.unisport.service;

import com.unisport.dto.NotificationQueryDTO;
import com.unisport.vo.NotificationListVO;

/**
 * Notification module service contract.
 */
public interface NotificationService {

    /**
     * Query notification list for current user with optional type filter and pagination.
     *
     * @param queryDTO query conditions
     * @return paged notification data and unread count
     */
    NotificationListVO listNotifications(NotificationQueryDTO queryDTO);

    /**
     * Mark a single notification as read.
     *
     * @param notificationId notification id
     */
    void markAsRead(Long notificationId);

    /**
     * Mark all notifications of current user as read.
     */
    void markAllAsRead();

    /**
     * Get unread notification count for current user.
     *
     * @return unread count
     */
    Long getUnreadCount();
}

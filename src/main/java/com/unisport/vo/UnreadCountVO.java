package com.unisport.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Response body for unread notification count.
 */
@Data
public class UnreadCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未读通知数量
     */
    private Long unreadCount = 0L;
}

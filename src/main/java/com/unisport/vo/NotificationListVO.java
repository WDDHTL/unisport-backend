package com.unisport.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通知列表响应对象，包含分页记录与未读总数。
 */
@Data
public class NotificationListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页通知记录。
     */
    private List<NotificationVO> records = Collections.emptyList();

    /**
     * 未读通知数量。
     */
    private Long unreadCount = 0L;

    /**
     * 总记录数。
     */
    private Long total = 0L;

    /**
     * 当前页码。
     */
    private Long current = 1L;

    /**
     * 每页大小。
     */
    private Long size = 20L;

    /**
     * 总页数。
     */
    private Long pages = 0L;
}

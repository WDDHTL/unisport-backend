package com.unisport.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 通知列表查询参数。
 * <p>对应文档中 GET /api/notifications 的入参，支持类型筛选与分页。</p>
 */
@Data
public class NotificationQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知类型过滤条件；支持 like/comment/follow/system/all，默认 all 不做过滤。
     */
    private String type = "all";

    /**
     * 当前页码，从 1 开始。
     */
    private Integer current = 1;

    /**
     * 每页大小，默认 20。
     */
    private Integer size = 20;
}

package com.unisport.dto;

import lombok.Data;

/**
 * 查询邀请广场列表的请求参数封装。
 */
@Data
public class InviteListQueryDTO {

    /**
     * 运动分类，可选。
     */
    private Long categoryId;

    /**
     * 状态过滤，支持 open,full,finished,canceled,expired 逗号分隔。
     */
    private String status;

    /**
     * 当前页码，从 1 开始。
     */
    private Integer current;

    /**
     * 每页大小。
     */
    private Integer size;

    /**
     * 排序字段描述，默认 created_at desc。
     */
    private String order;

    /**
     * 是否过滤已过期活动。
     */
    private Boolean excludeExpired;
}

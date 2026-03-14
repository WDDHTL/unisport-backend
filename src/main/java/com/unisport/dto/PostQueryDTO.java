package com.unisport.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子列表查询参数
 */
@Data
public class PostQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运动分类ID
     */
    private Integer categoryId;

    /**
     * 游标时间（上一页最后一条的 created_at）
     */
    private LocalDateTime cursorTime;

    /**
     * 游标ID（与游标时间配合，解决同一时间戳的并发写入排序）
     */
    private Long cursorId;

    /**
     * 每次请求条数，默认 10，最大 50
     */
    private Integer size = 10;
}

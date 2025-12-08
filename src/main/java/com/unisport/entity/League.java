package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 联赛实体类
 *
 * @author UniSport Team
 */
@Data
@TableName("leagues")
public class League implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 联赛ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 运动分类ID
     */
    private Integer categoryId;

    /**
     * 联赛名称
     */
    private String name;

    /**
     * 赛季年份
     */
    private Integer year;

    /**
     * 联赛描述
     */
    private String description;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 状态：1-进行中, 0-已结束
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

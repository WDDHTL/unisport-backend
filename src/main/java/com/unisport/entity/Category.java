package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运动分类实体类
 */
@Data
@TableName("categories")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 分类代码（football/basketball等）
     */
    private String code;

    /**
     * 分类名称（足球/篮球等）
     */
    private String name;

    /**
     * 图标 emoji
     */
    private String icon;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用, 0-禁用
     */
    private Integer status;
}

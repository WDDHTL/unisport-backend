package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 邀请活动实体。
 */
@Data
@TableName("invites")
public class Invite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发起人用户ID。
     */
    private Long hostId;

    /**
     * 学校ID，用于校园隔离。
     */
    private Long schoolId;

    /**
     * 运动分类。
     */
    private Integer categoryId;

    /**
     * 标题，可选。
     */
    private String title;

    /**
     * 活动描述。
     */
    private String description;

    /**
     * 活动日期。
     */
    private LocalDate activityDate;

    /**
     * 活动时间。
     */
    private LocalTime activityTime;

    /**
     * 活动地点。
     */
    private String location;

    /**
     * 最多人数。
     */
    private Integer maxPlayers;

    /**
     * 已加入人数。
     */
    private Integer joinedCount;

    /**
     * 状态：open/full/finished/canceled。
     */
    private String status;

    /**
     * 分享 token。
     */
    private String shareToken;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

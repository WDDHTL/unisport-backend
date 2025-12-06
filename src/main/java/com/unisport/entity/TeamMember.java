package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 队伍成员实体类
 *
 * @author UniSport Team
 */
@Data
@TableName("team_members")
public class TeamMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 队伍ID
     */
    private Long teamId;

    /**
     * 用户ID（可选关联，仅用于数据关联）
     */
    private Long userId;

    /**
     * 球员姓名（管理员录入）
     */
    private String playerName;

    /**
     * 球衣号码
     */
    private Integer jerseyNumber;

    /**
     * 位置（FW/MF/DF/GK等）
     */
    private String position;

    /**
     * 加入时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinedAt;
}

package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 比赛事件实体类
 *
 * @author UniSport Team
 */
@Data
@TableName("match_events")
public class MatchEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 比赛ID
     */
    private Long matchId;

    /**
     * 事件类型：goal-进球, yellow_card-黄牌, red_card-红牌, substitution-换人, whistle-哨声, other-其他
     */
    private String eventType;

    /**
     * 发生时间（分钟）
     */
    private Integer minute;

    /**
     * 相关队伍：A-A队, B-B队, neutral-中立
     */
    private String teamSide;

    /**
     * 相关球员ID（关联team_members表）
     */
    private Long playerId;

    /**
     * 事件描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

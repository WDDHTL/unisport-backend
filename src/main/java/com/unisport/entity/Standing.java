package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分榜实体类
 *
 * @author UniSport Team
 */
@Data
@TableName("standings")
public class Standing implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 联赛ID
     */
    private Long leagueId;

    /**
     * 队伍ID（团队项目）
     */
    private Long teamId;

    /**
     * 用户ID（个人项目）
     */
    private Long userId;

    /**
     * 队伍/用户名称
     */
    private String teamName;

    /**
     * 排名
     */
    @TableField("`rank`")
    private Integer rank;

    /**
     * 已赛场次
     */
    private Integer played;

    /**
     * 胜场
     */
    private Integer won;

    /**
     * 平局
     */
    private Integer drawn;

    /**
     * 负场
     */
    private Integer lost;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

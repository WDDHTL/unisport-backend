package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 比赛实体类
 *
 * @author UniSport Team
 */
@Data
@TableName("matches")
public class Match implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 比赛ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 联赛ID
     */
    private Long leagueId;

    /**
     * 运动分类ID
     */
    private Integer categoryId;

    /**
     * A队ID
     */
    private Long teamAId;

    /**
     * B队ID
     */
    private Long teamBId;

    /**
     * A选手ID（单人项目）
     */
    private Long playerAId;

    /**
     * B选手ID（单人项目）
     */
    private Long playerBId;

    /**
     * A方名称
     */
    private String teamAName;

    /**
     * B方名称
     */
    private String teamBName;

    /**
     * A方得分
     */
    private Integer scoreA;

    /**
     * B方得分
     */
    private Integer scoreB;

    /**
     * 比赛状态：upcoming-未开始, live-进行中, finished-已结束
     */
    private String status;

    /**
     * 比赛时间
     */
    private LocalDateTime matchTime;

    /**
     * 比赛地点
     */
    private String location;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

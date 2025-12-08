package com.unisport.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 积分榜视图对象
 *
 * @author UniSport Team
 */
@Data
public class StandingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 队伍/用户名称
     */
    private String teamName;

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
}

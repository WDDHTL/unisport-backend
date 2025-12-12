package com.unisport.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 比赛查询参数
 *
 * @author UniSport Team
 */
@Data
public class MatchQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运动分类代码（all表示全部）
     */
    private Integer categoryId;

    private Integer leagueId;

    private Integer schoolId;

    /**
     * 比赛状态：upcoming/live/finished/all
     */
    private String status = "all";

}

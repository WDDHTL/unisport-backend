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

    /**
     * 比赛状态：upcoming/live/finished/all
     */
    private String status = "all";

    /**
     * 页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}

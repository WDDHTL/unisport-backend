package com.unisport.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 比赛列表视图对象
 *
 * @author UniSport Team
 */
@Data
public class MatchVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 比赛ID
     */
    private Long id;

    /**
     * 运动分类代码
     */
    private String categoryCode;

    /**
     * 运动分类名称
     */
    private String categoryName;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime matchTime;

    /**
     * 比赛地点
     */
    private String location;
}

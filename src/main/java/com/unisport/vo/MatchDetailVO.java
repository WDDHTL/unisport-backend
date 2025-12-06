package com.unisport.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 比赛详情视图对象
 *
 * @author UniSport Team
 */
@Data
public class MatchDetailVO implements Serializable {

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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime matchTime;

    /**
     * 比赛地点
     */
    private String location;

    /**
     * A方球员阵容
     */
    private List<PlayerVO> playersA;

    /**
     * B方球员阵容
     */
    private List<PlayerVO> playersB;

    /**
     * 比赛事件时间轴
     */
    private List<EventVO> events;

    /**
     * 球员信息VO
     */
    @Data
    public static class PlayerVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 球员ID
         */
        private Long id;

        /**
         * 球员姓名
         */
        private String playerName;

        /**
         * 球衣号码
         */
        private Integer jerseyNumber;

        /**
         * 位置
         */
        private String position;
    }

    /**
     * 比赛事件VO
     */
    @Data
    public static class EventVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 事件ID
         */
        private Long id;

        /**
         * 事件类型：goal-进球, yellow_card-黄牌, red_card-红牌等
         */
        private String eventType;

        /**
         * 发生时间（分钟）
         */
        private Integer minute;

        /**
         * 事件描述
         */
        private String description;
    }
}

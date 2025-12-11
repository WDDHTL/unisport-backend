package com.unisport.service;

import com.unisport.vo.StandingVO;

import java.util.List;

/**
 * 积分榜服务接口
 *
 * @author UniSport Team
 */
public interface StandingService {

    /**
     * 获取积分榜
     *
     * @param categoryId 运动分类代码
     * @param year 年份（可选，默认当前年份）
     * @return 积分榜列表
     */
    List<StandingVO> getStandings(Integer categoryId, Integer year);
}

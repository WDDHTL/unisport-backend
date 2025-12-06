package com.unisport.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unisport.dto.MatchQueryDTO;
import com.unisport.vo.MatchVO;

/**
 * 比赛服务接口
 *
 * @author UniSport Team
 */
public interface MatchService {

    /**
     * 分页查询比赛列表
     *
     * @param queryDTO 查询参数
     * @return 比赛分页数据
     */
    Page<MatchVO> getMatchList(MatchQueryDTO queryDTO);
}

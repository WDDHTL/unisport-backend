package com.unisport.service;

import com.unisport.vo.PlayerStatsVO;

import java.util.List;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2025/12/8$
 */
public interface PlayerService {
    List<PlayerStatsVO> getStats(Integer categoryId, Integer year);
}

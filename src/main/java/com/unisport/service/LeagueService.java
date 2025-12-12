package com.unisport.service;

import com.unisport.vo.LeagueVO;

import java.util.List;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2025/12/12$
 */
public interface LeagueService {
    List<LeagueVO> getLeagueList(Long categoryId, Long schoolId);
}

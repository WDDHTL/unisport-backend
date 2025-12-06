package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.Match;
import org.apache.ibatis.annotations.Mapper;

/**
 * 比赛Mapper接口
 *
 * @author UniSport Team
 */
@Mapper
public interface MatchMapper extends BaseMapper<Match> {
}

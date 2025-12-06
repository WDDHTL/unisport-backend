package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.MatchEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 比赛事件Mapper接口
 *
 * @author UniSport Team
 */
@Mapper
public interface MatchEventMapper extends BaseMapper<MatchEvent> {
}

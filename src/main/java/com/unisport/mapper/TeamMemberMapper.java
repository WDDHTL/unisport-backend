package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 队伍成员Mapper接口
 *
 * @author UniSport Team
 */
@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
}

package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper for user follow relationships.
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
}

package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.Invite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邀请活动数据访问。
 */
@Mapper
public interface InviteMapper extends BaseMapper<Invite> {
}

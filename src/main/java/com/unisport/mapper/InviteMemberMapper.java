package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.InviteMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邀请成员数据访问。
 */
@Mapper
public interface InviteMemberMapper extends BaseMapper<InviteMember> {
}

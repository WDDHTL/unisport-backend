package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 提供用户数据访问层操作
 *
 * @author UniSport Team
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
}

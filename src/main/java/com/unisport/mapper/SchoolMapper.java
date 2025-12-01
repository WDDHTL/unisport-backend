package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.School;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校信息Mapper接口
 * 提供学校数据访问层操作
 *
 * @author UniSport Team
 */
@Mapper
public interface SchoolMapper extends BaseMapper<School> {
    
}

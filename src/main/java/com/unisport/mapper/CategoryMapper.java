package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 运动分类Mapper接口
 * 提供运动分类数据访问层操作
 *
 * @author UniSport Team
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
}

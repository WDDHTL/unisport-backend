package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学院信息Mapper接口
 * 提供学院数据访问层操作
 *
 * @author UniSport Team
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    
}

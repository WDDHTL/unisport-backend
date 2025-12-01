package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生信息Mapper接口
 * 提供学生身份验证数据访问
 *
 * @author UniSport Team
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    
}

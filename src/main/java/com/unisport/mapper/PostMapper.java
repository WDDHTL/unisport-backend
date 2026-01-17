package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子数据访问层
 * 基于MyBatis Plus实现，提供基础CRUD操作
 *
 * @author UniSport Team
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
    void deleteByPostIds(@Param("ids")List<Long> ids);
}

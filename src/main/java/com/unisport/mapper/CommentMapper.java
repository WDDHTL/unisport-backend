package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.Category;
import com.unisport.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/14$
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    int deleteByPostIds(@Param("postIds") List<Long> postIds);

}

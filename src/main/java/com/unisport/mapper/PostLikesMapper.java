package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.Category;
import com.unisport.entity.PostLikes;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/12$
 */
@Mapper
public interface PostLikesMapper extends BaseMapper<PostLikes> {
    
    @Delete("delete from post_likes where post_id = #{id} and user_id = #{userId};")
    void deleteByPostIdAndUserId(Long id, Long userId);

    int deleteByPostIds(@Param("postIds") List<Long> postIds);
}

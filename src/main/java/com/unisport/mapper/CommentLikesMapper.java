package com.unisport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unisport.entity.Comment;
import com.unisport.entity.CommentLikes;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/19$
 */
@Mapper
public interface CommentLikesMapper extends BaseMapper<CommentLikes> {
    /**
     * 根据评论id和用户id删除评论点赞记录
     * @param id 帖子id
     * @param userId 用户id
     */
    @Delete("delete from comment_likes where comment_id = #{id} and user_id = #{userId};")
    void deleteByCommentIdAndUserId(Long id, Long userId);

    void deleteByCommentIds(List<Long> commentIds);
}

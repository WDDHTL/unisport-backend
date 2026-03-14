package com.unisport.service;

import com.unisport.common.ScrollPageResult;
import com.unisport.dto.CommentDTO;
import com.unisport.dto.CreatePostDTO;
import com.unisport.dto.PostQueryDTO;
import com.unisport.entity.Post;
import com.unisport.vo.PostVO;

/**
 * 帖子服务接口
 * 定义帖子相关业务操作
 *
 * @author UniSport Team
 */
public interface PostService {

    /**
     * 发布帖子
     *
     * @param createPostDTO 发布帖子请求数据
     * @return 发布成功的帖子实体
     */
    Post createPost(CreatePostDTO createPostDTO);

    ScrollPageResult<PostVO> getPostList(PostQueryDTO postQueryDTO);

    void post_Likes(Long id);

    void post_UnLikes(Long id);

    PostVO getDetailById(Long id);

    void createComment(Long id, CommentDTO commentDTO);

    void deletePost(Long id);
}

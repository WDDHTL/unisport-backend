package com.unisport.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unisport.dto.CreatePostDTO;
import com.unisport.dto.PostQueryDTO;
import com.unisport.entity.Post;
import com.unisport.vo.PostVO;

import java.util.List;

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

    List<PostVO> getPostList(PostQueryDTO postQueryDTO);
}

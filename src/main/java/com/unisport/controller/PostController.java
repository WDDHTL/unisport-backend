package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.dto.CreatePostDTO;
import com.unisport.entity.Post;
import com.unisport.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 * 处理帖子相关的HTTP请求
 *
 * @author UniSport Team
 */
@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "帖子管理", description = "帖子发布、查询、点赞、评论等功能")
public class PostController {

    private final PostService postService;

    /**
     * 发布帖子
     * 
     * 接口说明：
     * - 需要登录认证（JWT Token）
     * - 内容长度限制：1-5000字符
     * - 图片数量限制：最多9张
     * - 频率限制：1分钟内最多发布3条
     * 
     * 业务流程：
     * 1. 从JWT Token获取当前用户ID
     * 2. 验证运动分类是否存在
     * 3. 检查发帖频率限制
     * 4. 创建帖子记录
     * 5. 返回帖子实体
     *
     * @param createPostDTO 发布帖子请求数据
     * @return 发布成功的帖子实体
     */
    @PostMapping
    @Operation(summary = "发布帖子", description = "用户发布新帖子")
    public Result<Post> createPost(@Valid @RequestBody CreatePostDTO createPostDTO) {
        log.info("接收发布帖子请求，分类：{}", createPostDTO.getCategoryCode());
        
        Post post = postService.createPost(createPostDTO);
        
        return Result.success(post);
    }
}

package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.common.ScrollPageResult;
import com.unisport.dto.CommentDTO;
import com.unisport.dto.CreatePostDTO;
import com.unisport.dto.PostQueryDTO;
import com.unisport.entity.Post;
import com.unisport.service.PostService;
import com.unisport.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 帖子控制器
 */
@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "帖子管理", description = "帖子发布、查询、点赞、评论等功能")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "发布帖子", description = "用户发布新帖子")
    public Result<Post> createPost(@Valid @RequestBody CreatePostDTO createPostDTO) {
        log.info("接收发布帖子请求，分类ID：{}", createPostDTO.getCategoryId());
        Post post = postService.createPost(createPostDTO);
        return Result.success(post);
    }

    @GetMapping
    @Operation(summary = "查询帖子列表", description = "获取帖子列表，支持滚动分页")
    public Result<ScrollPageResult<PostVO>> listPosts(
            @Parameter(description = "运动分类ID", example = "1")
            @RequestParam(required = true) Integer categoryId,
            @Parameter(description = "游标时间，上一页最后一条的 created_at", example = "2026-03-13T09:59:50Z")
            @RequestParam(value = "cursor_time", required = false) LocalDateTime cursorTime,
            @Parameter(description = "游标ID，上一页最后一条的 id", example = "122")
            @RequestParam(value = "cursor_id", required = false) Long cursorId,
            @Parameter(description = "每次请求条数", example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        log.info("接收查询帖子列表请求，分类ID：{}", categoryId);
        PostQueryDTO postQueryDTO = new PostQueryDTO();
        postQueryDTO.setCategoryId(categoryId);
        postQueryDTO.setCursorTime(cursorTime);
        postQueryDTO.setCursorId(cursorId);
        postQueryDTO.setSize(size);

        ScrollPageResult<PostVO> postVOs = postService.getPostList(postQueryDTO);
        return Result.success(postVOs);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞帖子", description = "用户点赞帖子")
    public Result<Void> post_Likes(@PathVariable Long id) {
        postService.post_Likes(id);
        return Result.success();
    }

    @Operation(summary = "取消点赞帖子", description = "用户取消点赞帖子")
    @DeleteMapping("/{id}/like")
    public Result<Void> post_UnLikes(@PathVariable Long id) {
        postService.post_UnLikes(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询帖子详情", description = "获取帖子详情")
    public Result<PostVO> getPost(@PathVariable Long id) {
        log.info("接收查询帖子详情请求，帖子ID：{}", id);
        PostVO postVO = postService.getDetailById(id);
        return Result.success(postVO);
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "发表评论", description = "用户发表评论")
    public Result<Void> createComment(@PathVariable Long id, @Valid @RequestBody CommentDTO commentDTO) {
        log.info("接收发表评论请求，帖子ID：{}", id);
        postService.createComment(id, commentDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除帖子", description = "用户删除帖子")
    public Result<Void> deletePost(@PathVariable Long id) {
        log.info("接收删除帖子请求，帖子ID：{}", id);
        postService.deletePost(id);
        return Result.success();
    }
}

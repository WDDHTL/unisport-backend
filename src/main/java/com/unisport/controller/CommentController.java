package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.dto.CommentDTO;
import com.unisport.service.CommentService;
import com.unisport.vo.CommentLikesVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/19$
 */
@Slf4j
@Tag(name = "评论管理", description = "评论分类相关接口")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{id}/reply")
    @Operation(summary = "回复评论", description = "用户回复评论")
    public Result replyComment(@PathVariable Long id, @Valid @RequestBody CommentDTO commentDTO) {
        log.info("接收回复评论请求，评论ID：{}", id);
        commentService.replyComment(id, commentDTO);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞评论", description = "用户点赞评论")
    public Result<CommentLikesVO> comment_Likes(@PathVariable Long id){
        log.info("点赞评论请求，评论ID：{}", id);
        CommentLikesVO commentLikesVO = commentService.comment_Likes(id);
        return Result.success(commentLikesVO);
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "取消点赞评论", description = "用户取消点赞评论")
    public Result<CommentLikesVO> comment_unLikes(@PathVariable Long id){
        log.info("取消点赞评论请求，评论ID：{}", id);
        CommentLikesVO commentLikesVO = commentService.comment_unLikes(id);
        return Result.success(commentLikesVO);
    }

}

package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.entity.Comment;
import com.unisport.entity.Post;
import com.unisport.mapper.CommentLikesMapper;
import com.unisport.mapper.CommentMapper;
import com.unisport.mapper.PostLikesMapper;
import com.unisport.mapper.PostMapper;
import com.unisport.service.CommentPurgeService;
import com.unisport.service.PostPurgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/17$
 */
@Service
@RequiredArgsConstructor
public class CommentPurgeServiceImpl implements CommentPurgeService {

    private final CommentMapper commentMapper;
    private final CommentLikesMapper commentLikesMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purgeExpiredDeletedComments(int retentionDays) {
        LocalDateTime deleteTime = LocalDateTime.now().minusDays(retentionDays);

        // 筛选待删除的Comment
        LambdaQueryWrapper<Comment> qw = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDeleted, 1)
                .isNotNull(Comment::getDeletedAt)
                .lt(Comment::getDeletedAt, deleteTime)
                .orderByAsc(Comment::getDeletedAt);
        List<Comment> comments = commentMapper.selectList(qw);

        // 待删除的评论id列表
        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());


        commentMapper.deleteByCommentIds(commentIds);

        // 后续还要添加删除评论点赞信息
        commentLikesMapper.deleteByCommentIds(commentIds);

    }
}

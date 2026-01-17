package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.unisport.entity.Post;
import com.unisport.mapper.CommentMapper;
import com.unisport.mapper.PostLikesMapper;
import com.unisport.mapper.PostMapper;
import com.unisport.service.PostPurgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class PostPurgeServiceImpl implements PostPurgeService {

    private PostMapper postMapper;
    private CommentMapper commentMapper;
    private PostLikesMapper postLikesMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purgeExpiredDeletedPosts(int retentionDays) {
        LocalDateTime deleteTime = LocalDateTime.now().minusDays(retentionDays);

        // 筛选待删除的Post
        LambdaQueryWrapper<Post> qw = new LambdaQueryWrapper<Post>()
                .eq(Post::getDeleted, 1)
                .isNotNull(Post::getDeletedAt)
                .lt(Post::getDeletedAt, deleteTime)
                .orderByAsc(Post::getDeletedAt);

        List<Post> posts = postMapper.selectList(qw);

        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        commentMapper.deleteByPostIds(postIds);

        postMapper.deleteByPostIds(postIds);

        postLikesMapper.deleteByPostIds(postIds);

        // TODO: 后续还要添加删除评论点赞信息
    }
}

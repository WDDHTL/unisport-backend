package com.unisport.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.unisport.Enum.NotifyType;
import com.unisport.Enum.RelatedType;
import com.unisport.WebSocket.WebSocketServer;
import com.unisport.common.BusinessException;
import com.unisport.common.UserContext;
import com.unisport.dto.CommentDTO;
import com.unisport.entity.Comment;
import com.unisport.entity.Notification;
import com.unisport.entity.Post;
import com.unisport.entity.User;
import com.unisport.mapper.CommentMapper;
import com.unisport.mapper.NotificationMapper;
import com.unisport.mapper.PostMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/19$
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final WebSocketServer webSocketServer;


    // TODO 当前版本仅通知 被回复评论的 用户
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyComment(Long id, CommentDTO commentDTO) {
        // 根据id查询帖子
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(40401, "评论不存在");
        }
        Long postId = comment.getPostId();
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException(40401, "帖子不存在");
        }

        //获取登录用户
        Long userId = UserContext.getUserId();

        // 构造新回复
        Comment newComment = new Comment();
        newComment.setPostId(postId);
        newComment.setUserId(userId);
        newComment.setParentId(id);
        newComment.setContent(commentDTO.getContent());
        commentMapper.insert(newComment);

        // 评论+1
        postMapper.update(
                null,
                new UpdateWrapper<Post>()
                        .eq("id", postId)
                        .setSql("comments_count = comments_count + 1")
        );

        // 构造ws通知
        // 收件人
        Long recipientId = comment.getUserId();
        // 发件人
        User user = userMapper.selectById(userId);

        if (!userId.equals(recipientId)){
            String content = buildCommentPreview(user.getNickname(),comment.getContent());
            Notification n = new Notification();
            n.setUserId(recipientId);            // 收件人
            n.setSenderId(userId);              // 触发者
            n.setType(NotifyType.COMMENT);
            n.setRelatedType(RelatedType.COMMENT);            // 关联对象类型
            n.setRelatedId(id);              // 关联对象 id
            n.setContent(content); // 展示文案（列表第二行）
            n.setIsRead(0);                      // 未读
            n.setCreatedAt(LocalDateTime.now());

            notificationMapper.insert(n);

            Long count = notificationMapper.selectCount(
                    new LambdaQueryWrapper<Notification>()
                            .eq(Notification::getUserId, recipientId)
                            .eq(Notification::getIsRead, 0)   // 或 eq(Notification::getRead, false)
            );

            // 基于ws连接推送点赞信息
            HashMap map = new HashMap();
            map.put("type",NotifyType.COMMENT);
            map.put("postId",postId);
            map.put("commentId",comment.getId());
            map.put("content",content);
            map.put("count", count);

            String jsonStr = JSONUtil.toJsonStr(map);

            webSocketServer.trySendToUser(recipientId, jsonStr);
        }


    }

    /*
     * 构造评论提示文案
     * */
    private String buildCommentPreview(String nickname, String postContent) {
        String text = (postContent == null) ? "" : postContent.trim();
        int maxLen = 20;
        if (text.length() > maxLen) {
            text = text.substring(0, maxLen) + "...";
        }
        return nickname + "回复了你的评论 \"" + text + "\"";
    }
}

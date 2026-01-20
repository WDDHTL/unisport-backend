package com.unisport.service;

import com.unisport.dto.CommentDTO;
import com.unisport.vo.CommentLikesVO;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/19$
 */
public interface CommentService {
    void replyComment(Long id, CommentDTO commentDTO);

    CommentLikesVO comment_Likes(Long id);

    CommentLikesVO comment_unLikes(Long id);

    void deleteComment(Long id);
}

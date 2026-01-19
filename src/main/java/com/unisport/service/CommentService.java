package com.unisport.service;

import com.unisport.dto.CommentDTO;

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
}

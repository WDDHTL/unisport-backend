package com.unisport.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/19$
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikesVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean Liked;
    private Integer likesCount;
}

package com.unisport.common;

import lombok.Data;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/12$
 */
@Data
public class LikePostResult {
    private Long postId;
    private Boolean alreadyLiked; // true=重复点赞（这次没产生变化）

    public static LikePostResult liked(Long postId) {
        LikePostResult r = new LikePostResult();
        r.setPostId(postId);
        r.setAlreadyLiked(false);
        return r;
    }

    public static LikePostResult alreadyLiked(Long postId) {
        LikePostResult r = new LikePostResult();
        r.setPostId(postId);
        r.setAlreadyLiked(true);
        return r;
    }
}

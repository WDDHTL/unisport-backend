package com.unisport.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/14$
 */
@Data
public class NewPostVO extends PostVO{
    // 标记是否已经点赞（1 点赞 0 没点赞）
    private Integer hasLiked;
}

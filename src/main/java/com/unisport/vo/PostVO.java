package com.unisport.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2025/12/11$
 */
@Data
public class PostVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;
    //1
    private String userName;
    //2
    private String userAvatar;

    private Long schoolId;
    //3
    private String schoolName;

    private Integer categoryId;

    private String content;

    private String images;

    private Integer likesCount;

    private Integer commentsCount;
    // 4
    private boolean isLiked;

    private LocalDateTime createdAt;
}

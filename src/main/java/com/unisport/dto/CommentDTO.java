package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/16$
 */
@Data
@Schema(description = "添加评论请求体")
public class CommentDTO {
    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父级评论ID")
    private Long parentId;
}

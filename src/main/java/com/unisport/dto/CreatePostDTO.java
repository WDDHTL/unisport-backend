package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发布帖子请求DTO
 * 用于接收前端发布帖子表单数据
 *
 * @author UniSport Team
 */
@Data
@Schema(description = "发布帖子请求对象")
public class CreatePostDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运动分类代码（football/basketball等）
     */
    @Schema(description = "运动分类代码", example = "football")
    @NotBlank(message = "运动分类不能为空")
    private String categoryCode;

    /**
     * 帖子内容
     */
    @Schema(description = "帖子内容", example = "今天的比赛太精彩了！")
    @NotBlank(message = "帖子内容不能为空")
    @Size(min = 1, max = 5000, message = "帖子内容长度必须在1-5000个字符之间")
    private String content;

    /**
     * 图片URL列表（最多9张）
     */
    @Schema(description = "图片URL列表", example = "[\"https://example.com/upload/123.jpg\"]")
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;
}

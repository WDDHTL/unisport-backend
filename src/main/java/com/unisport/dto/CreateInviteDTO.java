package com.unisport.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 创建邀请请求体。
 */
@Data
@Schema(description = "创建邀请请求")
public class CreateInviteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "运动分类ID", example = "1")
    @NotNull(message = "运动分类不能为空")
    private Long categoryId;

    @Schema(description = "活动日期，格式yyyy-MM-dd", example = "2025-05-25")
    @NotNull(message = "活动日期不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate activityDate;

    @Schema(description = "活动时间，格式HH:mm", example = "18:00")
    @NotNull(message = "活动时间不能为空")
    @DateTimeFormat(pattern = "HH:mm")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime activityTime;

    @Schema(description = "活动地点", example = "北操场")
    @NotBlank(message = "活动地点不能为空")
    @Size(max = 100, message = "活动地点长度不能超过100个字符")
    private String location;

    @Schema(description = "活动说明", example = "周末轻松踢球，守门已有安排")
    @NotBlank(message = "活动说明不能为空")
    @Size(max = 500, message = "活动说明长度不能超过500个字符")
    private String description;

    @Schema(description = "最大人数，2-50", example = "8")
    @NotNull(message = "最大人数不能为空")
    @Min(value = 2, message = "最大人数不能少于2人")
    @Max(value = 50, message = "最大人数不能超过50人")
    private Integer maxPlayers;

    @Schema(description = "邀请标题（可选）", example = "周末踢球")
    @Size(max = 50, message = "标题长度不能超过50个字符")
    private String title;

    @Schema(description = "分享token（可选）", example = "ABCD1234")
    @Size(max = 32, message = "分享token长度不能超过32个字符")
    private String shareToken;
}

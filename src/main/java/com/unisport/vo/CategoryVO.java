package com.unisport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 运动分类VO（返回给前端的数据对象）
 * 
 * @author UniSport Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "运动分类信息")
public class CategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID")
    private Integer id;

    @Schema(description = "分类代码（如：football, basketball）")
    private String code;

    @Schema(description = "分类名称（如：足球、篮球）")
    private String name;

    @Schema(description = "图标emoji（如：⚽、🏀）")
    private String icon;

    @Schema(description = "排序顺序")
    private Integer sortOrder;
}

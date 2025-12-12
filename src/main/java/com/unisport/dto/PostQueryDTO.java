package com.unisport.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2025/12/11$
 */
@Data
public class PostQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运动分类ID
     */
    private Integer categoryId;

    /**
     * 页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}

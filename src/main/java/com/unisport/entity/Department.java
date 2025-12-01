package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 学院信息实体类
 *
 * @author UniSport Team
 */
@Data
@TableName("departments")
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学院ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学校ID
     */
    private Long schoolId;

    /**
     * 学院名称
     */
    private String name;

    /**
     * 学院代码
     */
    private String code;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用, 0-禁用
     */
    private Integer status;
}

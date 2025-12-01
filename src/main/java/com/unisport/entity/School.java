package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 学校信息实体类
 *
 * @author UniSport Team
 */
@Data
@TableName("schools")
public class School implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学校ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学校名称
     */
    private String name;

    /**
     * 学校代码
     */
    private String code;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用, 0-禁用
     */
    private Integer status;
}

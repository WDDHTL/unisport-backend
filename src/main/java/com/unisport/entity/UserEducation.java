package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户教育经历实体
 */
@Data
@TableName("user_educations")
public class UserEducation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教育经历ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 学校ID
     */
    private Long schoolId;

    /**
     * 学校名称（冗余，便于展示）
     */
    @TableField(exist = false)
    private String school;

    /**
     * 学院ID
     */
    private Long departmentId;

    /**
     * 学院名称（冗余，便于展示）
     */
    @TableField(exist = false)
    private String department;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 入学时间（YYYY-MM）
     */
    private String startDate;

    /**
     * 结束时间（YYYY-MM），null 表示至今
     */
    private String endDate;

    /**
     * 是否为主要教育经历
     */
    private Boolean isPrimary;

    /**
     * 学号验证状态：pending/verified/failed
     */
    private String status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}

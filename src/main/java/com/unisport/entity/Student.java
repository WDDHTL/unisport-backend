package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生信息实体类
 * 用于验证学生身份
 *
 * @author UniSport Team
 */
@Data
@TableName("students")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 学校ID
     */
    private Long schoolId;

    /**
     * 学院ID
     */
    private Long departmentId;

    /**
     * 状态：1-在校, 0-已毕业/离校
     */
    private Integer status;

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
}

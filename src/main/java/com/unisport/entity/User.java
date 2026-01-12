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
 * User entity mapped to the users table.
 */
@Data
@TableName("users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary education school id (derived from user_educations, not persisted).
     */
    @TableField(exist = false)
    private Long schoolId;

    /**
     * Primary education school name (derived, not persisted).
     */
    @TableField(exist = false)
    private String school;

    /**
     * Primary education department name (derived, not persisted).
     */
    @TableField(exist = false)
    private String department;

    /** User ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** Account (student id or phone) */
    private String account;

    /** Password (encrypted) */
    private String password;

    /** Nickname */
    private String nickname;

    /** Avatar URL */
    private String avatar;

    /** Student id */
    private String studentId;

    /** Personal bio */
    private String bio;

    /** Gender: 1-male, 0-female */
    private Integer gender;

    /** Account status: 1-active, 0-banned */
    private Integer status;

    /** Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** Logical delete flag */
    @TableLogic
    private Integer deleted;
}

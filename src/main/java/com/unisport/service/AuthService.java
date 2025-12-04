package com.unisport.service;

import com.unisport.dto.LoginDTO;
import com.unisport.dto.RegisterDTO;
import com.unisport.vo.LoginVO;
import com.unisport.vo.RegisterVO;

/**
 * 认证授权服务接口
 * 提供用户注册、登录等功能
 *
 * @author UniSport Team
 */
public interface AuthService {

    /**
     * 用户注册
     * 
     * 注册流程：
     * 1. 校验账号唯一性
     * 2. 验证学生身份（学号、学校、学院信息匹配）
     * 3. 密码加密存储
     * 4. 创建用户记录
     *
     * @param registerDTO 注册请求数据
     * @return 注册成功的用户信息
     */
    RegisterVO register(RegisterDTO registerDTO);

    /**
     * 用户登录
     * 
     * 登录流程：
     * 1. 根据账号查询用户信息
     * 2. 验证用户是否存在
     * 3. 验证账号状态是否正常
     * 4. 验证密码是否正确（BCrypt比对）
     * 5. 生成JWT Token
     * 6. 返回Token和用户基本信息
     *
     * @param loginDTO 登录请求数据
     * @return 登录成功信息（Token和用户信息）
     */
    LoginVO login(LoginDTO loginDTO);
}

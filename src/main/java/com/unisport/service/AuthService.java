package com.unisport.service;

import com.unisport.dto.RegisterDTO;
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
}

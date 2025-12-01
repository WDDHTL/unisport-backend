package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.dto.RegisterDTO;
import com.unisport.service.AuthService;
import com.unisport.vo.RegisterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证授权控制器
 * 处理用户注册、登录等认证相关请求
 *
 * @author UniSport Team
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证授权模块", description = "用户注册、登录、登出等认证授权接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册接口
     * 
     * 接口说明：
     * - 基于学号验证学生身份（软绑定）
     * - 密码使用BCrypt加密存储
     * - 注册成功后默认昵称为账号
     * - 需要提供学校、学院、学号信息进行身份验证
     *
     * @param registerDTO 注册请求参数
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，需要验证学号身份")
    public Result<RegisterVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("收到用户注册请求，账号：{}", registerDTO.getAccount());
        
        // 调用服务层处理注册逻辑
        RegisterVO registerVO = authService.register(registerDTO);
        
        log.info("用户注册成功，用户ID：{}，账号：{}", registerVO.getId(), registerVO.getAccount());
        return Result.success("注册成功", registerVO);
    }
}

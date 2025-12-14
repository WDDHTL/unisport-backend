package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.dto.UpdateUserDTO;
import com.unisport.service.UserService;
import com.unisport.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User info endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户模块", description = "用户信息接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "用于用户主页、帖子作者信息展示")
    public Result<UserProfileVO> getUserProfile(@PathVariable("id") Long userId) {
        log.info("接到获取用户信息请求，userId={}", userId);
        UserProfileVO profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "只能修改自己的基础信息，未提交字段保持不变")
    public Result<UserProfileVO> updateUserProfile(@PathVariable("id") Long userId,
                                                   @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        log.info("接到更新用户信息请求，userId={}", userId);
        UserProfileVO profile = userService.updateUserProfile(userId, updateUserDTO);
        return Result.success(profile);
    }
}

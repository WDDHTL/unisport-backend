package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.service.UserService;
import com.unisport.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息相关接口。
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
        log.info("收到获取用户信息请求，userId={}", userId);
        UserProfileVO profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }
}

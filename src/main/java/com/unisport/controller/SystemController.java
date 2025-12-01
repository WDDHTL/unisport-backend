package com.unisport.controller;

import com.unisport.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统健康检查控制器
 */
@Tag(name = "系统管理", description = "系统健康检查和基础信息")
@RestController
@RequestMapping("/system")
public class SystemController {

    @Operation(summary = "健康检查", description = "检查系统运行状态")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "UP");
        info.put("application", "UniSport Backend");
        info.put("version", "1.0.0");
        info.put("timestamp", System.currentTimeMillis());
        return Result.success(info);
    }

    @Operation(summary = "系统信息", description = "获取系统基础信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "UniSport 校园体育社交应用");
        info.put("description", "基于 Spring Boot 3.2 的后端服务");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("springBootVersion", "3.2.0");
        return Result.success(info);
    }
}

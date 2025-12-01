package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.service.DepartmentService;
import com.unisport.vo.DepartmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学院控制器
 * 处理学院信息查询相关请求
 *
 * @author UniSport Team
 */
@Slf4j
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统功能模块-学院管理", description = "学院信息查询接口")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 根据学校ID获取学院列表
     * 
     * 接口说明：
     * - 根据学校ID查询该学校下的所有学院
     * - 只返回启用状态的学院
     * - 按排序字段排序
     * - 用于注册时选择学院
     *
     * @param schoolId 学校ID（必填）
     * @return 学院信息列表
     */
    @GetMapping
    @Operation(summary = "获取学院列表", description = "根据学校ID查询该学校下的所有启用学院")
    public Result<List<DepartmentVO>> getDepartmentList(
            @Parameter(description = "学校ID（必填）", example = "1", required = true)
            @RequestParam @NotNull(message = "学校ID不能为空") Long schoolId) {
        
        log.info("收到获取学院列表请求，学校ID：{}", schoolId);
        
        // 调用服务层查询学院列表
        List<DepartmentVO> departmentList = departmentService.getDepartmentListBySchoolId(schoolId);
        
        log.info("成功返回 {} 个学院", departmentList.size());
        return Result.success(departmentList);
    }
}

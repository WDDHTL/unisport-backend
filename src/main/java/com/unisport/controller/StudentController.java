package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 学生控制器
 * 处理学生信息查询和验证相关请求
 *
 * @author UniSport Team
 */
@Slf4j
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统功能模块-学生管理", description = "学生信息查询和验证接口")
public class StudentController {

    private final StudentService studentService;

    /**
     * 验证学号是否存在
     * 
     * 接口说明：
     * - 验证学号、学校ID、学院ID是否匹配
     * - 检查学生是否在校（status=1）
     * - 用于注册时实时验证学号
     * - 仅返回验证结果，不返回学生详细信息
     *
     * @param studentId 学号（必填）
     * @param schoolId 学校ID（必填）
     * @param departmentId 学院ID（必填）
     * @return 验证结果
     */
    @GetMapping("/validate")
    @Operation(summary = "验证学号是否存在", 
               description = "用于注册时验证学号、学校、学院信息是否匹配，以及学生是否在校")
    public Result<Map<String, Object>> validateStudent(
            @RequestParam 
            @NotBlank(message = "学号不能为空")
            @Parameter(description = "学号", required = true, example = "2024001001")
            String studentId,
            
            @RequestParam 
            @NotNull(message = "学校ID不能为空")
            @Parameter(description = "学校ID", required = true, example = "1")
            Long schoolId,
            
            @RequestParam 
            @NotNull(message = "学院ID不能为空")
            @Parameter(description = "学院ID", required = true, example = "1")
            Long departmentId) {
        
        log.info("验证学号: studentId={}, schoolId={}, departmentId={}", studentId, schoolId, departmentId);
        
        boolean valid = studentService.validateStudent(studentId, schoolId, departmentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);
        result.put("message", valid ? "学号验证通过" : "该学号不存在或学校/学院信息不匹配");
        
        log.info("学号验证结果: {}", valid);
        
        return Result.success(result);
    }
}

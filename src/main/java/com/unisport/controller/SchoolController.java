package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.service.SchoolService;
import com.unisport.vo.SchoolVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学校控制器
 * 处理学校信息查询相关请求
 *
 * @author UniSport Team
 */
@Slf4j
@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
@Tag(name = "系统功能模块-学校管理", description = "学校信息查询接口")
public class SchoolController {

    private final SchoolService schoolService;

    /**
     * 获取学校列表
     * 
     * 接口说明：
     * - 返回所有启用状态的学校
     * - 支持按省份和城市筛选
     * - 按排序字段排序
     * - 用于注册时选择学校
     *
     * @param province 省份筛选条件（可选）
     * @param city 城市筛选条件（可选）
     * @return 学校信息列表
     */
    @GetMapping
    @Operation(summary = "获取学校列表", description = "查询所有启用的学校，支持省份和城市筛选")
    public Result<List<SchoolVO>> getSchoolList(
            @Parameter(description = "省份筛选（可选）", example = "北京")
            @RequestParam(required = false) String province,
            
            @Parameter(description = "城市筛选（可选）", example = "北京市")
            @RequestParam(required = false) String city) {
        
        log.info("收到获取学校列表请求，省份：{}，城市：{}", province, city);
        
        // 调用服务层查询学校列表
        List<SchoolVO> schoolList = schoolService.getSchoolList(province, city);
        
        log.info("成功返回 {} 所学校", schoolList.size());
        return Result.success(schoolList);
    }
}

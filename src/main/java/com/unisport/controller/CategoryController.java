package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.service.CategoryService;
import com.unisport.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 运动分类控制器
 * 提供运动分类查询接口
 *
 * @author UniSport Team
 */
@Slf4j
@Tag(name = "运动分类管理", description = "运动分类相关接口")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取运动分类列表
     * 
     * 接口地址：GET /api/categories
     * 使用场景：
     * 1. 首页导航栏展示分类
     * 2. 发帖时选择分类
     * 3. 前端启动时获取一次即可（可缓存）
     * 
     * 返回数据说明：
     * - 按 sortOrder 排序
     * - 只返回启用状态的分类
     * - 包含分类ID、代码、名称、图标、排序顺序
     *
     * @return 运动分类列表
     */
    @Operation(
        summary = "获取运动分类列表",
        description = "获取所有启用的运动分类，按排序顺序返回。用于首页导航栏展示、发帖时选择分类等场景。"
    )
    @GetMapping
    public Result<List<CategoryVO>> getCategoryList() {
        log.info("收到获取运动分类列表请求");
        
        List<CategoryVO> categoryList = categoryService.getCategoryList();
        
        log.info("返回 {} 个运动分类", categoryList.size());
        return Result.success(categoryList);
    }
}

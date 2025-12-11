package com.unisport.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unisport.common.Result;
import com.unisport.dto.MatchQueryDTO;
import com.unisport.service.MatchService;
import com.unisport.vo.MatchDetailVO;
import com.unisport.vo.MatchVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 比赛控制器
 *
 * @author UniSport Team
 */
@Slf4j
@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
@Tag(name = "赛事模块", description = "比赛相关接口")
public class MatchController {

    private final MatchService matchService;

    /**
     * 获取比赛列表
     *
     * @param categoryId 运动分类id
     * @param status 比赛状态（可选，默认all）
     * @param current 页码（可选，默认1）
     * @param size 每页大小（可选，默认10）
     * @return 比赛分页数据
     */
    @GetMapping
    @Operation(summary = "获取比赛列表", description = "支持按分类、状态筛选，分页查询")
    public Result<Page<MatchVO>> getMatchList(
        @Parameter(description = "运动分类代码", example = "football") 
        @RequestParam(required = true) Integer categoryId,
        
        @Parameter(description = "比赛状态：upcoming/live/finished/all", example = "upcoming") 
        @RequestParam(required = false, defaultValue = "all") String status,
        
        @Parameter(description = "页码", example = "1") 
        @RequestParam(required = false, defaultValue = "1") Integer current,
        
        @Parameter(description = "每页大小", example = "10") 
        @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        MatchQueryDTO queryDTO = new MatchQueryDTO();
        queryDTO.setCategoryId(categoryId);
        queryDTO.setStatus(status);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);

        Page<MatchVO> page = matchService.getMatchList(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取比赛详情
     *
     * @param id 比赛ID
     * @return 比赛详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取比赛详情", description = "查看比赛的详细信息、球员阵容和比赛事件")
    public Result<MatchDetailVO> getMatchDetail(
        @Parameter(description = "比赛ID", example = "1")
        @PathVariable Long id
    ) {
        log.info("请求查询比赛详情，ID：{}", id);
        MatchDetailVO detail = matchService.getMatchDetail(id);
        
        if (detail == null) {
            return Result.error(40401, "比赛不存在");
        }
        
        return Result.success(detail);
    }

}

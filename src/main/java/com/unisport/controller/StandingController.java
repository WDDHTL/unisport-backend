package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.service.StandingService;
import com.unisport.vo.StandingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分榜控制器
 *
 * @author UniSport Team
 */
@Slf4j
@RestController
@RequestMapping("/standings")
@RequiredArgsConstructor
@Tag(name = "赛事模块", description = "积分榜相关接口")
public class StandingController {

    private final StandingService standingService;

    /**
     * 获取积分榜
     *
     * @param categoryCode 运动分类代码（必填）
     * @param year 年份（可选，默认当前年份）
     * @return 积分榜列表
     */
    @GetMapping
    @Operation(summary = "获取积分榜", description = "查看联赛积分排名")
    public Result<List<StandingVO>> getStandings(
        @Parameter(description = "运动分类代码", example = "football", required = true)
        @RequestParam String categoryCode,
        
        @Parameter(description = "年份，默认当前年份", example = "2025")
        @RequestParam(required = false) Integer year
    ) {
        log.info("请求查询积分榜，分类：{}，年份：{}", categoryCode, year);
        List<StandingVO> standings = standingService.getStandings(categoryCode, year);
        return Result.success(standings);
    }
}

package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.service.PlayerService;
import com.unisport.vo.PlayerStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2025/12/8$
 */
@Slf4j
@RestController
@RequestMapping("/player-stats")
@RequiredArgsConstructor
@Tag(name = "赛事模块", description = "球员相关接口")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    @Operation(summary = "获取球员榜", description = "查看球员排名")
    public Result<List<PlayerStatsVO>> getPlayerStats(
            @Parameter(description = "运动分类代码", example = "football", required = true)
            @RequestParam String categoryCode,

            @Parameter(description = "年份，默认当前年份", example = "2025")
            @RequestParam(required = false) Integer year
    ) {
        log.info("请求查询球员榜，分类：{}，年份：{}", categoryCode, year);
        List<PlayerStatsVO> stats = playerService.getStats(categoryCode, year);
        return Result.success(stats);
    }
}

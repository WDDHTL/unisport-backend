package com.unisport.controller;

import cn.hutool.db.PageResult;
import com.unisport.common.Result;
import com.unisport.entity.League;
import com.unisport.service.LeagueService;
import com.unisport.vo.LeagueVO;
import io.swagger.v3.oas.annotations.Operation;
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
 * @since 2025/12/12$
 */
@Slf4j
@RestController
@RequestMapping("/leagues")
@RequiredArgsConstructor
@Tag(name = "联赛模块", description = "联赛相关接口")
public class LeagueController {

    private final LeagueService leagueService;

    /**
     * 获取联赛列表
     *
     * @param categoryId 运动分类id
     * @return 联赛分页数据
     */
    @GetMapping
    @Operation(summary = "获取联赛列表")
    public Result<List<LeagueVO>> getLeagueList(@RequestParam(required = true) Long categoryId, @RequestParam(required = false) Long schoolId) {
        log.info("请求查询联赛列表，分类ID：{}", categoryId);
        List<LeagueVO> leagueList = leagueService.getLeagueList(categoryId, schoolId);
        return Result.success(leagueList);
    }

}

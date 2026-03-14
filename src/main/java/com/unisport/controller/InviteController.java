package com.unisport.controller;

import com.unisport.common.PageResult;
import com.unisport.common.Result;
import com.unisport.dto.CreateInviteDTO;
import com.unisport.dto.InviteListQueryDTO;
import com.unisport.dto.InviteMineQueryDTO;
import com.unisport.service.InviteService;
import com.unisport.vo.InviteListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邀请相关接口。
 */
@Slf4j
@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
@Tag(name = "邀请广场", description = "邀请广场列表等接口")
public class InviteController {

    private final InviteService inviteService;

    @PostMapping
    @Operation(summary = "创建邀请", description = "发起新的活动邀请")
    public Result<InviteListVO> createInvite(@Valid @RequestBody CreateInviteDTO request) {
        log.info("接收创建邀请请求，分类={}, 日期={}, 时间={}", request.getCategoryId(), request.getActivityDate(), request.getActivityTime());
        InviteListVO invite = inviteService.createInvite(request);
        return Result.success(invite);
    }

    @GetMapping
    @Operation(summary = "获取邀请广场列表", description = "根据分类、状态分页查询邀请列表，默认过滤已过期活动")
    public Result<PageResult<InviteListVO>> listInvites(
            @Parameter(description = "运动分类ID", example = "1")
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @Parameter(description = "状态过滤，支持 open,full 组合", example = "open,full")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(value = "current", required = false, defaultValue = "1") Integer current,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @Parameter(description = "排序字段", example = "created_at desc")
            @RequestParam(value = "order", required = false, defaultValue = "created_at desc") String order,
            @Parameter(description = "是否过滤已过期", example = "true")
            @RequestParam(value = "excludeExpired", required = false, defaultValue = "true") Boolean excludeExpired
    ) {
        InviteListQueryDTO query = new InviteListQueryDTO();
        query.setCategoryId(categoryId);
        query.setStatus(status);
        query.setCurrent(current);
        query.setSize(size);
        query.setOrder(order);
        query.setExcludeExpired(excludeExpired);

        log.info("接收邀请广场列表查询，请求参数：{}", query);
        PageResult<InviteListVO> page = inviteService.listInvites(query);
        return Result.success(page);
    }

    @GetMapping("/mine")
    @Operation(summary = "获取我的邀请", description = "查询我发起或参与的邀请列表")
    public Result<PageResult<InviteListVO>> listMyInvites(
            @Parameter(description = "查看范围，host/joined/all", example = "all")
            @RequestParam(value = "view", required = false, defaultValue = "all") String view,
            @Parameter(description = "状态过滤，open/full/finished/canceled/expired/all", example = "all")
            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(value = "current", required = false, defaultValue = "1") Integer current,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        InviteMineQueryDTO query = new InviteMineQueryDTO();
        query.setView(view);
        query.setStatus(status);
        query.setCurrent(current);
        query.setSize(size);
        log.info("Query my invites, view={}, status={}, current={}, size={}", view, status, current, size);
        PageResult<InviteListVO> page = inviteService.listMyInvites(query);
        return Result.success(page);
    }
}

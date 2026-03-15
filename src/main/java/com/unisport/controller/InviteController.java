package com.unisport.controller;

import com.unisport.common.PageResult;
import com.unisport.common.Result;
import com.unisport.dto.CreateInviteDTO;
import com.unisport.dto.InviteListQueryDTO;
import com.unisport.dto.InviteMineQueryDTO;
import com.unisport.dto.JoinInviteDTO;
import com.unisport.service.InviteService;
import com.unisport.vo.InviteDetailVO;
import com.unisport.vo.InviteListVO;
import com.unisport.vo.InviteMemberDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Invite related APIs.
 */
@Slf4j
@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
@Tag(name = "Invite Center", description = "Invite listing, mine, create, join")
public class InviteController {

    private final InviteService inviteService;

    @PostMapping
    @Operation(summary = "Create invite", description = "Host a new activity invite")
    public Result<InviteListVO> createInvite(@Valid @RequestBody CreateInviteDTO request) {
        log.info("Create invite request received, category={}, date={}, time={}", request.getCategoryId(), request.getActivityDate(), request.getActivityTime());
        InviteListVO invite = inviteService.createInvite(request);
        return Result.success(invite);
    }

    @GetMapping
    @Operation(summary = "List invites", description = "Query invite list by category and status, excluding expired by default")
    public Result<PageResult<InviteListVO>> listInvites(
            @Parameter(description = "Category ID", example = "1")
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @Parameter(description = "Status filter, support open/full combination", example = "open,full")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Current page", example = "1")
            @RequestParam(value = "current", required = false, defaultValue = "1") Integer current,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @Parameter(description = "Order by field", example = "created_at desc")
            @RequestParam(value = "order", required = false, defaultValue = "created_at desc") String order,
            @Parameter(description = "Exclude expired", example = "true")
            @RequestParam(value = "excludeExpired", required = false, defaultValue = "true") Boolean excludeExpired
    ) {
        InviteListQueryDTO query = new InviteListQueryDTO();
        query.setCategoryId(categoryId);
        query.setStatus(status);
        query.setCurrent(current);
        query.setSize(size);
        query.setOrder(order);
        query.setExcludeExpired(excludeExpired);

        log.info("Query invite list, params: {}", query);
        PageResult<InviteListVO> page = inviteService.listInvites(query);
        return Result.success(page);
    }

    @GetMapping("/mine")
    @Operation(summary = "List my invites", description = "Query invites I created or joined")
    public Result<PageResult<InviteListVO>> listMyInvites(
            @Parameter(description = "View scope: host/joined/all", example = "all")
            @RequestParam(value = "view", required = false, defaultValue = "all") String view,
            @Parameter(description = "Status filter: open/full/finished/canceled/expired/all", example = "all")
            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
            @Parameter(description = "Current page", example = "1")
            @RequestParam(value = "current", required = false, defaultValue = "1") Integer current,
            @Parameter(description = "Page size", example = "10")
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

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get invite detail", description = "Fetch invite detail with member list, allow anonymous access for landing page")
    public Result<InviteDetailVO> getInviteDetail(
            @Parameter(description = "Invite ID", required = true) @PathVariable("id") Long id
    ) {
        log.info("Query invite detail, inviteId={}", id);
        InviteDetailVO detail = inviteService.getInviteDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/{id:\\d+}/members")
    @Operation(summary = "List invite members", description = "Fetch active members by invite ID with pagination")
    public Result<PageResult<InviteMemberDetailVO>> listInviteMembers(
            @Parameter(description = "Invite ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "Current page", example = "1")
            @RequestParam(value = "current", required = false, defaultValue = "1") Integer current,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        log.info("Query invite members, inviteId={}, current={}, size={}", id, current, size);
        PageResult<InviteMemberDetailVO> members = inviteService.listInviteMembers(id, current, size);
        return Result.success("success", members);
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Join invite", description = "Join invite by ID, prevent duplicate join and overbooking")
    public Result<InviteListVO> joinInvite(
            @Parameter(description = "Invite ID", required = true) @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Join request body, optional", required = false)
            @RequestBody(required = false) JoinInviteDTO request
    ) {
        log.info("Join invite request received, inviteId={}", id);
        InviteListVO invite = inviteService.joinInvite(id, request);
        return Result.success(invite);
    }

    @DeleteMapping("/{id}/join")
    @Operation(summary = "Leave invite", description = "Leave invite by ID, decrease join count and reopen if needed")
    public Result<InviteListVO> leaveInvite(
            @Parameter(description = "Invite ID", required = true) @PathVariable("id") Long id
    ) {
        log.info("Leave invite request received, inviteId={}", id);
        InviteListVO invite = inviteService.leaveInvite(id);
        return Result.success(invite);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel invite", description = "Cancel invite by host, mark status as canceled")
    public Result<InviteListVO> cancelInvite(
            @Parameter(description = "Invite ID", required = true) @PathVariable("id") Long id
    ) {
        log.info("Cancel invite request received, inviteId={}", id);
        InviteListVO invite = inviteService.cancelInvite(id);
        return Result.success(invite);
    }
}

package com.unisport.controller;

import com.unisport.common.PageResult;
import com.unisport.common.Result;
import com.unisport.dto.WechatExchangeAcceptDTO;
import com.unisport.dto.WechatExchangeCreateDTO;
import com.unisport.dto.WechatExchangeListQueryDTO;
import com.unisport.dto.WechatExchangeRejectDTO;
import com.unisport.service.WechatExchangeService;
import com.unisport.vo.WechatExchangeRequestVO;
import com.unisport.vo.WechatExchangeStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Wechat exchange related APIs.
 */
@Slf4j
@RestController
@RequestMapping("/wechat-exchange")
@RequiredArgsConstructor
@Tag(name = "微信交换", description = "用户之间交换微信号相关接口")
public class WechatExchangeController {

    private final WechatExchangeService wechatExchangeService;

    @PostMapping("/requests")
    @Operation(summary = "发起交换请求", description = "A 向 B 发起交换微信请求")
    public Result<WechatExchangeRequestVO> createRequest(@Valid @RequestBody WechatExchangeCreateDTO request) {
        log.info("Create wechat exchange request, targetId={}", request.getTargetId());
        WechatExchangeRequestVO vo = wechatExchangeService.createRequest(request);
        return Result.success(vo);
    }

    @GetMapping("/requests")
    @Operation(summary = "请求列表", description = "按角色和状态查询收到/发出的微信交换请求")
    public Result<PageResult<WechatExchangeRequestVO>> listRequests(
            @Parameter(description = "角色：received/sent", example = "received")
            @RequestParam(value = "role", required = false, defaultValue = "received") String role,
            @Parameter(description = "状态过滤，逗号分隔", example = "pending,accepted")
            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
            @Parameter(description = "当前页", example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Parameter(description = "分页大小", example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        WechatExchangeListQueryDTO query = new WechatExchangeListQueryDTO();
        query.setRole(role);
        query.setStatus(status);
        query.setCurrent(page);
        query.setSize(size);
        log.info("List wechat exchange requests, role={}, status={}, page={}, size={}", role, status, page, size);
        PageResult<WechatExchangeRequestVO> result = wechatExchangeService.listRequests(query);
        return Result.success(result);
    }

    @GetMapping("/requests/{id}")
    @Operation(summary = "请求详情", description = "仅发起方或接收方可查看详情")
    public Result<WechatExchangeRequestVO> getRequestDetail(@PathVariable("id") Long id) {
        log.info("Get wechat exchange detail, id={}", id);
        WechatExchangeRequestVO vo = wechatExchangeService.getRequestDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/requests/{id}/accept")
    @Operation(summary = "同意交换", description = "接收方同意并写入微信号快照")
    public Result<WechatExchangeStatusVO> accept(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) WechatExchangeAcceptDTO request
    ) {
        log.info("Accept wechat exchange request, id={}", id);
        WechatExchangeStatusVO vo = wechatExchangeService.accept(id, request);
        return Result.success(vo);
    }

    @PostMapping("/requests/{id}/reject")
    @Operation(summary = "拒绝交换", description = "接收方拒绝交换请求，可填写原因")
    public Result<WechatExchangeStatusVO> reject(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) WechatExchangeRejectDTO request
    ) {
        log.info("Reject wechat exchange request, id={}", id);
        WechatExchangeStatusVO vo = wechatExchangeService.reject(id, request);
        return Result.success(vo);
    }

    @PostMapping("/requests/{id}/cancel")
    @Operation(summary = "撤销请求", description = "发起方在待处理时撤销请求")
    public Result<WechatExchangeStatusVO> cancel(@PathVariable("id") Long id) {
        log.info("Cancel wechat exchange request, id={}", id);
        WechatExchangeStatusVO vo = wechatExchangeService.cancel(id);
        return Result.success(vo);
    }
}

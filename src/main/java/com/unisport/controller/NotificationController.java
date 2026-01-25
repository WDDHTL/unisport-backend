package com.unisport.controller;

import com.unisport.common.Result;
import com.unisport.dto.NotificationQueryDTO;
import com.unisport.service.NotificationService;
import com.unisport.vo.NotificationListVO;
import com.unisport.vo.UnreadCountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息通知相关接口。
 */
@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "消息通知", description = "通知列表、已读状态等接口")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取通知列表。
     * <p>文档要求：支持类型筛选(type=like/comment/follow/all)、分页(current/size)，返回记录及未读数量。</p>
     *
     * @param type    通知类型过滤，默认 all
     * @param current 页码，默认 1
     * @param size    页大小，默认 20
     * @return 通知列表及未读数量
     */
    @GetMapping
    @Operation(summary = "获取通知列表", description = "按照类型与分页条件返回通知列表，并附带未读总数")
    public Result<NotificationListVO> listNotifications(
            @RequestParam(value = "type", defaultValue = "all") String type,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {

        log.info("接收通知列表查询请求，type={}, current={}, size={}", type, current, size);
        NotificationQueryDTO queryDTO = new NotificationQueryDTO();
        queryDTO.setType(type);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);

        NotificationListVO data = notificationService.listNotifications(queryDTO);
        return Result.success(data);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将指定通知标记为已读")
    public Result<Void> markAsRead(@PathVariable("id") Long id) {
        log.info("标记通知已读，请求id={}", id);
        notificationService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部已读", description = "将当前用户的所有通知标记为已读")
    public Result<Void> markAllAsRead() {
        log.info("标记当前用户所有通知为已读");
        notificationService.markAllAsRead();
        return Result.success();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读数量", description = "获取当前用户的未读通知数量")
    public Result<UnreadCountVO> unreadCount() {
        Long count = notificationService.getUnreadCount();
        UnreadCountVO vo = new UnreadCountVO();
        vo.setUnreadCount(count);
        return Result.success(vo);
    }
}

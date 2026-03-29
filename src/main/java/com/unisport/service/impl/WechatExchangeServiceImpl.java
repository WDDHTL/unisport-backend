package com.unisport.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unisport.Enum.NotifyType;
import com.unisport.Enum.RelatedType;
import com.unisport.Enum.WechatExchangeStatus;
import com.unisport.WebSocket.WebSocketServer;
import com.unisport.common.BusinessException;
import com.unisport.common.PageResult;
import com.unisport.common.UserContext;
import com.unisport.config.JwtProperties;
import com.unisport.dto.WechatExchangeAcceptDTO;
import com.unisport.dto.WechatExchangeCreateDTO;
import com.unisport.dto.WechatExchangeListQueryDTO;
import com.unisport.dto.WechatExchangeRejectDTO;
import com.unisport.entity.Notification;
import com.unisport.entity.User;
import com.unisport.entity.WechatExchangeRequest;
import com.unisport.mapper.NotificationMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.mapper.WechatExchangeRequestMapper;
import com.unisport.service.WechatExchangeService;
import com.unisport.utils.AesUtil;
import com.unisport.vo.SimpleUserVO;
import com.unisport.vo.WechatExchangeRequestVO;
import com.unisport.vo.WechatExchangeStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Wechat exchange domain service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatExchangeServiceImpl implements WechatExchangeService {

    private static final Duration REQUEST_TTL = Duration.ofHours(72);
    private static final String ROLE_SENT = "sent";
    private static final String ROLE_RECEIVED = "received";
    private static final int DAILY_TARGET_LIMIT = 3;
    private static final int DAILY_TOTAL_LIMIT = 10;

    private final WechatExchangeRequestMapper exchangeRequestMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final JwtProperties jwtProperties;
    private final WebSocketServer webSocketServer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatExchangeRequestVO createRequest(WechatExchangeCreateDTO request) {
        Long requesterId = requireLogin();
        Long targetId = request.getTargetId();
        if (targetId == null) {
            throw new BusinessException(40004, "目标用户ID不能为空");
        }
        if (requesterId.equals(targetId)) {
            throw new BusinessException(40004, "不能向自己发起交换微信请求");
        }

        User requester = userMapper.selectById(requesterId);
        if (requester == null) {
            throw new BusinessException(40101, "登录状态已失效，请重新登录");
        }
        User target = userMapper.selectById(targetId);
        if (target == null) {
            throw new BusinessException(40401, "目标用户不存在");
        }

        String requesterWechatSnapshot = requester.getWechatId();
        String requesterWechatPlain = decryptWechat(requesterWechatSnapshot);
        if (!StringUtils.hasText(requesterWechatPlain)) {
            throw new BusinessException(40004, "请先在个人资料中设置微信号后再发起请求");
        }

        ensureNoPendingDuplicate(requesterId, targetId);
        validateRequestFrequency(requesterId, targetId);

        WechatExchangeRequest entity = new WechatExchangeRequest();
        entity.setRequesterId(requesterId);
        entity.setTargetId(targetId);
        entity.setStatus(WechatExchangeStatus.PENDING);
        entity.setSource(trimToNull(request.getSource()));
        entity.setRequesterWechatSnapshot(requesterWechatSnapshot);
        entity.setExpiredAt(LocalDateTime.now().plus(REQUEST_TTL));

        int rows = exchangeRequestMapper.insert(entity);
        if (rows <= 0 || entity.getId() == null) {
            throw new BusinessException(50001, "创建请求失败，请稍后重试");
        }

        String content = requester.getNickname() + " 请求和你交换微信";
        pushNotification(targetId, requesterId, NotifyType.WECHAT_EXCHANGE_REQUEST, entity.getId(), content);

        Map<Long, SimpleUserVO> userMap = toSimpleUserMap(List.of(requester, target));
        return toVO(entity, userMap, requesterId);
    }

    @Override
    public PageResult<WechatExchangeRequestVO> listRequests(WechatExchangeListQueryDTO query) {
        Long currentUserId = requireLogin();
        boolean sent = ROLE_SENT.equalsIgnoreCase(trimToDefault(query.getRole(), ROLE_RECEIVED));
        Set<WechatExchangeStatus> statusFilter = parseStatuses(query.getStatus());
        long pageNum = query.getCurrent() == null || query.getCurrent() <= 0 ? 1 : query.getCurrent();
        long pageSize = query.getSize() == null || query.getSize() <= 0 ? 10 : query.getSize();

        LambdaQueryWrapper<WechatExchangeRequest> wrapper = new LambdaQueryWrapper<>();
        if (sent) {
            wrapper.eq(WechatExchangeRequest::getRequesterId, currentUserId);
        } else {
            wrapper.eq(WechatExchangeRequest::getTargetId, currentUserId);
        }
        if (!CollectionUtils.isEmpty(statusFilter)) {
            wrapper.in(WechatExchangeRequest::getStatus, statusFilter);
        }
        wrapper.orderByDesc(WechatExchangeRequest::getCreatedAt);

        Page<WechatExchangeRequest> page = exchangeRequestMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<WechatExchangeRequest> refreshed = refreshExpiredIfNeeded(page.getRecords());

        Set<Long> userIds = new HashSet<>();
        refreshed.forEach(r -> {
            userIds.add(r.getRequesterId());
            userIds.add(r.getTargetId());
        });
        Map<Long, SimpleUserVO> userMap = resolveUsers(userIds);

        List<WechatExchangeRequestVO> records = refreshed.stream()
                .map(r -> toVO(r, userMap, currentUserId))
                .collect(Collectors.toList());

        return PageResult.of(pageNum, pageSize, page.getTotal(), page.getPages(), records);
    }

    @Override
    public WechatExchangeRequestVO getRequestDetail(Long id) {
        Long currentUserId = requireLogin();
        WechatExchangeRequest record = exchangeRequestMapper.selectById(id);
        if (record == null || (!currentUserId.equals(record.getRequesterId()) && !currentUserId.equals(record.getTargetId()))) {
            throw new BusinessException(40401, "记录不存在或无访问权限");
        }
        record = refreshExpiredIfNeeded(record);

        Set<Long> userIds = new HashSet<>(Arrays.asList(record.getRequesterId(), record.getTargetId()));
        Map<Long, SimpleUserVO> userMap = resolveUsers(userIds);
        return toVO(record, userMap, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatExchangeStatusVO accept(Long id, WechatExchangeAcceptDTO request) {
        Long currentUserId = requireLogin();
        WechatExchangeRequest record = loadOwnedRequest(id, currentUserId, true);
        record = refreshExpiredIfNeeded(record);
        if (record.getStatus() != WechatExchangeStatus.PENDING) {
            throw new BusinessException(40004, "当前状态不支持同意操作");
        }

        User target = userMapper.selectById(currentUserId);
        WechatSnapshot targetWechat = resolveTargetWechatSnapshot(request, target);
        LocalDateTime now = LocalDateTime.now();

        int rows = exchangeRequestMapper.update(
                null,
                new UpdateWrapper<WechatExchangeRequest>()
                        .eq("id", id)
                        .eq("target_id", currentUserId)
                        .eq("status", WechatExchangeStatus.PENDING)
                        .set("status", WechatExchangeStatus.ACCEPTED)
                        .set("target_wechat_snapshot", targetWechat.encrypted())
                        .set("responded_at", now)
                        .set("respond_message", null)
        );
        if (rows <= 0) {
            throw new BusinessException(50001, "操作失败，请稍后重试");
        }

        WechatExchangeRequest latest = exchangeRequestMapper.selectById(id);
        String requesterWechatPlain = decryptWechat(latest.getRequesterWechatSnapshot());
        String content = target.getNickname() + " 同意了你的微信交换请求，微信号：" + targetWechat.plain();
        pushNotification(latest.getRequesterId(), currentUserId, NotifyType.WECHAT_EXCHANGE_ACCEPT, id, content);

        String targetTip = "已同意并展示对方微信：" + requesterWechatPlain;
        pushNotification(latest.getTargetId(), currentUserId, NotifyType.WECHAT_EXCHANGE_ACCEPT, id, targetTip);

        return buildStatusVO(WechatExchangeStatus.ACCEPTED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatExchangeStatusVO reject(Long id, WechatExchangeRejectDTO request) {
        Long currentUserId = requireLogin();
        WechatExchangeRequest record = loadOwnedRequest(id, currentUserId, true);
        record = refreshExpiredIfNeeded(record);
        if (record.getStatus() != WechatExchangeStatus.PENDING) {
            throw new BusinessException(40004, "当前状态不支持拒绝操作");
        }

        String reason = trimToNull(request != null ? request.getReason() : null);
        LocalDateTime now = LocalDateTime.now();
        int rows = exchangeRequestMapper.update(
                null,
                new UpdateWrapper<WechatExchangeRequest>()
                        .eq("id", id)
                        .eq("target_id", currentUserId)
                        .eq("status", WechatExchangeStatus.PENDING)
                        .set("status", WechatExchangeStatus.REJECTED)
                        .set("responded_at", now)
                        .set("respond_message", reason)
        );
        if (rows <= 0) {
            throw new BusinessException(50001, "操作失败，请稍后重试");
        }

        User target = userMapper.selectById(currentUserId);
        String content = target.getNickname() + " 拒绝了你的微信交换请求" + (reason != null ? "，原因：" + reason : "");
        pushNotification(record.getRequesterId(), currentUserId, NotifyType.WECHAT_EXCHANGE_REJECT, id, content);

        return buildStatusVO(WechatExchangeStatus.REJECTED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatExchangeStatusVO cancel(Long id) {
        Long currentUserId = requireLogin();
        WechatExchangeRequest record = loadOwnedRequest(id, currentUserId, false);
        record = refreshExpiredIfNeeded(record);
        if (record.getStatus() != WechatExchangeStatus.PENDING) {
            throw new BusinessException(40004, "当前状态不支持撤销操作");
        }
        LocalDateTime now = LocalDateTime.now();
        int rows = exchangeRequestMapper.update(
                null,
                new UpdateWrapper<WechatExchangeRequest>()
                        .eq("id", id)
                        .eq("requester_id", currentUserId)
                        .eq("status", WechatExchangeStatus.PENDING)
                        .set("status", WechatExchangeStatus.CANCELLED)
                        .set("responded_at", now)
                        .set("respond_message", null)
        );
        if (rows <= 0) {
            throw new BusinessException(50001, "操作失败，请稍后重试");
        }

        String content = "对方已撤销微信交换请求";
        pushNotification(record.getTargetId(), currentUserId, NotifyType.WECHAT_EXCHANGE_REJECT, id, content);

        return buildStatusVO(WechatExchangeStatus.CANCELLED);
    }

    private void ensureNoPendingDuplicate(Long requesterId, Long targetId) {
        Long count = exchangeRequestMapper.selectCount(
                new LambdaQueryWrapper<WechatExchangeRequest>()
                        .eq(WechatExchangeRequest::getRequesterId, requesterId)
                        .eq(WechatExchangeRequest::getTargetId, targetId)
                        .eq(WechatExchangeRequest::getStatus, WechatExchangeStatus.PENDING)
        );
        if (count != null && count > 0) {
            throw new BusinessException(40901, "已有待处理的交换请求，请勿重复发送");
        }
    }

    /**
     * Frequency control to avoid abuse.
     */
    private void validateRequestFrequency(Long requesterId, Long targetId) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        Long perTargetCount = exchangeRequestMapper.selectCount(
                new LambdaQueryWrapper<WechatExchangeRequest>()
                        .eq(WechatExchangeRequest::getRequesterId, requesterId)
                        .eq(WechatExchangeRequest::getTargetId, targetId)
                        .ge(WechatExchangeRequest::getCreatedAt, startOfToday)
        );
        if (perTargetCount != null && perTargetCount >= DAILY_TARGET_LIMIT) {
            throw new BusinessException(42901, "今日对该用户的交换请求已达上限，请明日再试");
        }

        Long totalCount = exchangeRequestMapper.selectCount(
                new LambdaQueryWrapper<WechatExchangeRequest>()
                        .eq(WechatExchangeRequest::getRequesterId, requesterId)
                        .ge(WechatExchangeRequest::getCreatedAt, startOfToday)
        );
        if (totalCount != null && totalCount >= DAILY_TOTAL_LIMIT) {
            throw new BusinessException(42902, "今日交换微信请求次数已达上限，请明日再试");
        }
    }

    private List<WechatExchangeRequest> refreshExpiredIfNeeded(List<WechatExchangeRequest> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        List<WechatExchangeRequest> result = new ArrayList<>(records.size());
        for (WechatExchangeRequest record : records) {
            result.add(refreshExpiredIfNeeded(record));
        }
        return result;
    }

    private WechatExchangeRequest refreshExpiredIfNeeded(WechatExchangeRequest record) {
        if (record == null) {
            return null;
        }
        if (record.getStatus() == WechatExchangeStatus.PENDING
                && record.getExpiredAt() != null
                && record.getExpiredAt().isBefore(LocalDateTime.now())) {
            LocalDateTime now = LocalDateTime.now();
            int rows = exchangeRequestMapper.update(
                    null,
                    new UpdateWrapper<WechatExchangeRequest>()
                            .eq("id", record.getId())
                            .eq("status", WechatExchangeStatus.PENDING)
                            .set("status", WechatExchangeStatus.EXPIRED)
                            .set("responded_at", now)
            );
            if (rows > 0) {
                record.setStatus(WechatExchangeStatus.EXPIRED);
                record.setRespondedAt(now);
            }
        }
        return record;
    }

    private Map<Long, SimpleUserVO> resolveUsers(Set<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        return toSimpleUserMap(users);
    }

    private Map<Long, SimpleUserVO> toSimpleUserMap(List<User> users) {
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyMap();
        }
        return users.stream()
                .filter(Objects::nonNull)
                .map(this::toSimpleUserVO)
                .collect(Collectors.toMap(SimpleUserVO::getId, Function.identity(), (a, b) -> a));
    }

    private SimpleUserVO toSimpleUserVO(User user) {
        SimpleUserVO vo = new SimpleUserVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    private WechatExchangeRequestVO toVO(WechatExchangeRequest record, Map<Long, SimpleUserVO> userMap, Long currentUserId) {
        WechatExchangeRequestVO vo = new WechatExchangeRequestVO();
        vo.setId(record.getId());
        vo.setStatus(record.getStatus() != null ? record.getStatus().getValue() : null);
        vo.setSource(record.getSource());
        vo.setRespondMessage(record.getRespondMessage());
        vo.setExpiredAt(record.getExpiredAt());
        vo.setCreatedAt(record.getCreatedAt());
        vo.setRespondedAt(record.getRespondedAt());
        vo.setRequester(userMap.get(record.getRequesterId()));
        vo.setTarget(userMap.get(record.getTargetId()));

        if (record.getStatus() == WechatExchangeStatus.ACCEPTED
                && (currentUserId != null)
                && (currentUserId.equals(record.getRequesterId()) || currentUserId.equals(record.getTargetId()))) {
            String otherWechat;
            if (currentUserId.equals(record.getRequesterId())) {
                otherWechat = decryptWechat(record.getTargetWechatSnapshot());
            } else {
                otherWechat = decryptWechat(record.getRequesterWechatSnapshot());
            }
            vo.setOtherWechatId(otherWechat);
        }
        return vo;
    }

    private Set<WechatExchangeStatus> parseStatuses(String statusStr) {
        String normalized = trimToDefault(statusStr, "all");
        if ("all".equalsIgnoreCase(normalized)) {
            return Collections.emptySet();
        }
        Set<WechatExchangeStatus> result = new HashSet<>();
        String[] parts = normalized.split(",");
        for (String part : parts) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            try {
                result.add(WechatExchangeStatus.valueOf(part.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(40004, "状态不合法，支持 pending/accepted/rejected/cancelled/expired/all");
            }
        }
        return result;
    }

    private WechatSnapshot resolveTargetWechatSnapshot(WechatExchangeAcceptDTO request, User targetUser) {
        String provided = request != null ? trimToNull(request.getTargetWechatId()) : null;
        if (StringUtils.hasText(provided)) {
            return new WechatSnapshot(encryptWechat(provided), provided);
        }
        String storedEncrypted = targetUser.getWechatId();
        String storedPlain = decryptWechat(storedEncrypted);
        if (!StringUtils.hasText(storedPlain)) {
            throw new BusinessException(40004, "请先在个人资料中设置微信号");
        }
        return new WechatSnapshot(storedEncrypted, storedPlain);
    }

    private WechatExchangeRequest loadOwnedRequest(Long id, Long currentUserId, boolean asTarget) {
        WechatExchangeRequest record = exchangeRequestMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(40401, "记录不存在");
        }
        if (asTarget && !currentUserId.equals(record.getTargetId())) {
            throw new BusinessException(40301, "无权限处理该请求");
        }
        if (!asTarget && !currentUserId.equals(record.getRequesterId())) {
            throw new BusinessException(40301, "无权限处理该请求");
        }
        return record;
    }

    private void pushNotification(Long recipientId, Long senderId, NotifyType type, Long relatedId, String content) {
        Notification n = new Notification();
        n.setUserId(recipientId);
        n.setSenderId(senderId);
        n.setType(type);
        n.setRelatedType(RelatedType.WECHAT_EXCHANGE);
        n.setRelatedId(relatedId);
        n.setContent(content);
        n.setIsRead(0);
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);

        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, recipientId)
                        .eq(Notification::getIsRead, 0)
        );
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type.getValue());
        payload.put("relatedId", relatedId);
        payload.put("content", content);
        payload.put("count", count);
        webSocketServer.trySendToUser(recipientId, JSONUtil.toJsonStr(payload));
    }

    private WechatExchangeStatusVO buildStatusVO(WechatExchangeStatus status) {
        WechatExchangeStatusVO vo = new WechatExchangeStatusVO();
        vo.setStatus(status.getValue());
        return vo;
    }

    private String decryptWechat(String encrypted) {
        if (!StringUtils.hasText(encrypted)) {
            return null;
        }
        try {
            return AesUtil.decrypt(encrypted, jwtProperties.getSecret());
        } catch (Exception e) {
            log.warn("解密微信号失败，已忽略", e);
            return null;
        }
    }

    private String encryptWechat(String plaintext) {
        try {
            return AesUtil.encrypt(plaintext, jwtProperties.getSecret());
        } catch (Exception e) {
            log.warn("加密微信号失败", e);
            throw new BusinessException(50001, "微信号加密失败，请稍后再试");
        }
    }

    private Long requireLogin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        return userId;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private record WechatSnapshot(String encrypted, String plain) {
    }
}

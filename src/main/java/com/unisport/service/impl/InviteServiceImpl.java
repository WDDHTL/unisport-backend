package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unisport.common.BusinessException;
import com.unisport.common.PageResult;
import com.unisport.common.UserContext;
import com.unisport.dto.CreateInviteDTO;
import com.unisport.dto.InviteListQueryDTO;
import com.unisport.dto.InviteMineQueryDTO;
import com.unisport.dto.JoinInviteDTO;
import com.unisport.entity.Category;
import com.unisport.entity.Invite;
import com.unisport.entity.InviteMember;
import com.unisport.entity.User;
import com.unisport.mapper.CategoryMapper;
import com.unisport.mapper.InviteMapper;
import com.unisport.mapper.InviteMemberMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.service.InviteService;
import com.unisport.vo.InviteDetailVO;
import com.unisport.vo.InviteListVO;
import com.unisport.vo.InviteMemberBriefVO;
import com.unisport.vo.InviteMemberDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 邀请相关服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {

    private static final int DAILY_CREATE_LIMIT = 20;
    private static final Duration CREATE_LOCK_TTL = Duration.ofSeconds(5);
    private static final Duration JOIN_LOCK_TTL = Duration.ofSeconds(10);

    private final CategoryMapper categoryMapper;
    private final InviteMapper inviteMapper;
    private final InviteMemberMapper inviteMemberMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InviteListVO createInvite(CreateInviteDTO request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        Long schoolId = resolveSchoolId(userId);

        Category category = resolveCategoryById(request.getCategoryId());
        validateActivityDateTime(request.getActivityDate(), request.getActivityTime());
        validateMaxPlayers(request.getMaxPlayers());

        String lockKey = "lock:invite:create:" + userId;
        if (!acquireCreateLock(lockKey)) {
            throw new BusinessException(40901, "请勿重复提交");
        }

        boolean created = false;
        String quotaKey = null;
        long currentCount = 0L;
        try {
            quotaKey = buildCreateCountKey(userId);
            currentCount = incrementCreateCount(quotaKey);
            if (currentCount > DAILY_CREATE_LIMIT) {
                throw new BusinessException(40901, "发起过于频繁，请明日再试");
            }

            Invite invite = buildInviteEntity(request, userId, schoolId, category);
            int rows = inviteMapper.insert(invite);
            if (rows <= 0 || invite.getId() == null) {
                throw new BusinessException(50001, "创建邀请失败，请稍后重试");
            }

            saveHostMember(invite.getId(), userId);
            evictInviteCaches();

            InviteListVO vo = buildInviteVOs(Collections.singletonList(invite), userId)
                    .stream()
                    .findFirst()
                    .orElseGet(InviteListVO::new);
            created = true;
            return vo;
        } finally {
            if (!created && quotaKey != null && currentCount > 0 && currentCount <= DAILY_CREATE_LIMIT) {
                try {
                    stringRedisTemplate.opsForValue().decrement(quotaKey);
                } catch (Exception e) {
                    log.warn("回滚邀请发起计数失败，key={}", quotaKey, e);
                }
            }
            releaseCreateLock(lockKey);
        }
    }

    @Override
    public PageResult<InviteListVO> listInvites(InviteListQueryDTO query) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        Long schoolId = resolveSchoolId(userId);

        int current = query.getCurrent() == null || query.getCurrent() <= 0 ? 1 : query.getCurrent();
        int size = query.getSize() == null || query.getSize() <= 0 ? 10 : query.getSize();
        if (size > 50) {
            size = 50;
        }
        boolean excludeExpired = query.getExcludeExpired() == null || query.getExcludeExpired();
        Set<String> statusFilters = parseStatuses(query.getStatus());

        String cacheKey = buildCacheKey(schoolId, query.getCategoryId(), statusFilters, excludeExpired, current, size, query.getOrder(), userId);
        PageResult<InviteListVO> cached = readFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        Page<Invite> page = new Page<>(current, size);
        LambdaQueryWrapper<Invite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invite::getSchoolId, schoolId);
        Integer categoryId = resolveCategoryId(query.getCategoryId());
        if (categoryId != null) {
            wrapper.eq(Invite::getCategoryId, categoryId);
        } else if (query.getCategoryId() != null) {
            // 传入无效分类时直接返回空数据
            return PageResult.of(current, size, 0, 0, Collections.emptyList());
        }

        applyStatusFilter(wrapper, statusFilters);
        applyExpireFilter(wrapper, excludeExpired);
        applyOrder(wrapper, query.getOrder());

        Page<Invite> invitePage = inviteMapper.selectPage(page, wrapper);
        List<Invite> invites = invitePage.getRecords();

        List<InviteListVO> records = buildInviteVOs(invites, userId);
        PageResult<InviteListVO> pageResult = PageResult.of(current, size, invitePage.getTotal(), invitePage.getPages(), records);
        writeToCache(cacheKey, pageResult);
        return pageResult;
    }

    @Override
    public PageResult<InviteListVO> listMyInvites(InviteMineQueryDTO query) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        Long schoolId = resolveSchoolId(userId);

        int current = query.getCurrent() == null || query.getCurrent() <= 0 ? 1 : query.getCurrent();
        int size = query.getSize() == null || query.getSize() <= 0 ? 10 : query.getSize();
        if (size > 50) {
            size = 50;
        }
        String view = normalizeView(query.getView());
        Set<String> statuses = parseMineStatuses(query.getStatus());
        log.info("Query my invites, userId={}, view={}, status={}, page={}, size={}", userId, view, statuses, current, size);

        Page<Invite> page = new Page<>(current, size);
        LambdaQueryWrapper<Invite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invite::getSchoolId, schoolId);
        applyViewFilter(wrapper, view, userId);
        applyStatusFilterForMine(wrapper, statuses);
        applyMineOrder(wrapper, view);

        Page<Invite> invitePage = inviteMapper.selectPage(page, wrapper);
        List<InviteListVO> records = buildInviteVOs(invitePage.getRecords(), userId);
        return PageResult.of(current, size, invitePage.getTotal(), invitePage.getPages(), records);
    }

    @Override
    public InviteDetailVO getInviteDetail(Long inviteId) {
        if (inviteId == null || inviteId <= 0) {
            throw new BusinessException(40401, "邀请不存在");
        }
        Long userId = UserContext.getUserId();
        Long schoolId = UserContext.getSchoolId();

        String cacheKey = buildDetailCacheKey(inviteId, userId);
        InviteDetailVO cached = readDetailFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        Invite invite = inviteMapper.selectById(inviteId);
        validateInviteReadable(invite, schoolId);

        List<InviteMember> members = inviteMemberMapper.selectList(
                new LambdaQueryWrapper<InviteMember>()
                        .eq(InviteMember::getInviteId, inviteId)
                        .eq(InviteMember::getStatus, "active")
                        .orderByAsc(InviteMember::getRole)
                        .orderByAsc(InviteMember::getJoinedAt)
        );

        InviteListVO inviteVO = buildInviteVOs(Collections.singletonList(invite), userId)
                .stream()
                .findFirst()
                .orElseGet(InviteListVO::new);
        InviteDetailVO detail = new InviteDetailVO();
        detail.setInvite(inviteVO);
        detail.setMembers(buildMemberBriefs(members));
        writeDetailToCache(cacheKey, detail);
        return detail;
    }

    @Override
    public PageResult<InviteMemberDetailVO> listInviteMembers(Long inviteId, Integer currentParam, Integer sizeParam) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        if (inviteId == null || inviteId <= 0) {
            throw new BusinessException(40401, "邀请不存在");
        }
        Long schoolId = resolveSchoolId(userId);

        Invite invite = inviteMapper.selectById(inviteId);
        validateInviteReadable(invite, schoolId);

        int current = currentParam == null || currentParam <= 0 ? 1 : currentParam;
        int size = sizeParam == null || sizeParam <= 0 ? 20 : Math.min(sizeParam, 50);

        Page<InviteMember> page = new Page<>(current, size);
        LambdaQueryWrapper<InviteMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InviteMember::getInviteId, inviteId)
                .eq(InviteMember::getStatus, "active")
                .orderByAsc(InviteMember::getRole)
                .orderByAsc(InviteMember::getJoinedAt);
        Page<InviteMember> memberPage = inviteMemberMapper.selectPage(page, wrapper);
        List<InviteMember> members = memberPage.getRecords();

        List<InviteMemberDetailVO> records = buildMemberDetails(members);
        return PageResult.of(current, size, memberPage.getTotal(), memberPage.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InviteListVO joinInvite(Long inviteId, JoinInviteDTO request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        if (inviteId == null || inviteId <= 0) {
            throw new BusinessException(40401, "邀请不存在");
        }
        Long schoolId = resolveSchoolId(userId);

        String lockKey = buildJoinLockKey(inviteId);
        if (!acquireJoinLock(lockKey)) {
            throw new BusinessException(40901, "操作过于频繁，请稍后重试");
        }

        try {
            Invite invite = inviteMapper.selectById(inviteId);
            validateInviteJoinable(invite, schoolId);

            InviteMember existing = findMember(inviteId, userId);
            if (existing != null && "active".equalsIgnoreCase(existing.getStatus())) {
                throw new BusinessException(40024, "已加入，无需重复提交");
            }

            int maxPlayers = invite.getMaxPlayers() == null ? 0 : invite.getMaxPlayers();
            int joinedCount = invite.getJoinedCount() == null ? 0 : invite.getJoinedCount();
            if (maxPlayers <= 0) {
                throw new BusinessException(40004, "队伍配置异常");
            }
            if (joinedCount >= maxPlayers) {
                throw new BusinessException(40022, "邀请已满员");
            }

            boolean activated = false;
            boolean inserted = false;
            if (existing != null) {
                existing.setStatus("active");
                existing.setLeftAt(null);
                existing.setJoinedAt(LocalDateTime.now());
                int updated = inviteMemberMapper.updateById(existing);
                if (updated <= 0) {
                    throw new BusinessException(50001, "更新成员状态失败，请稍后重试");
                }
                activated = true;
            } else {
                InviteMember member = new InviteMember();
                member.setInviteId(inviteId);
                member.setUserId(userId);
                member.setRole("member");
                member.setStatus("active");
                member.setJoinedAt(LocalDateTime.now());
                try {
                    inviteMemberMapper.insert(member);
                    inserted = true;
                } catch (DuplicateKeyException e) {
                    log.warn("事务冲突，加入成员行被唯一约束拦截，inviteId={} userId={}", inviteId, userId, e);
                    InviteMember concurrent = findMember(inviteId, userId);
                    if (concurrent != null && "active".equalsIgnoreCase(concurrent.getStatus())) {
                        throw new BusinessException(40024, "已加入，无需重复提交");
                    }
                    if (concurrent != null) {
                        concurrent.setStatus("active");
                        concurrent.setLeftAt(null);
                        concurrent.setJoinedAt(LocalDateTime.now());
                        int updated = inviteMemberMapper.updateById(concurrent);
                        if (updated <= 0) {
                            throw new BusinessException(50001, "更新成员状态失败，请稍后重试");
                        }
                        activated = true;
                    } else {
                        throw new BusinessException(50001, "加入失败，请稍后重试");
                    }
                }
            }

            if (inserted || activated) {
                updateInviteJoinCount(inviteId, maxPlayers);
            }
            evictInviteCaches();

            Invite updated = inviteMapper.selectById(inviteId);
            return buildInviteVOs(Collections.singletonList(updated), userId)
                    .stream()
                    .findFirst()
                    .orElseGet(InviteListVO::new);
        } finally {
            releaseJoinLock(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InviteListVO leaveInvite(Long inviteId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        if (inviteId == null || inviteId <= 0) {
            throw new BusinessException(40401, "邀请不存在");
        }
        Long schoolId = resolveSchoolId(userId);

        String lockKey = buildJoinLockKey(inviteId);
        if (!acquireJoinLock(lockKey)) {
            throw new BusinessException(40901, "操作过于频繁，请稍后重试");
        }

        try {
            Invite invite = inviteMapper.selectById(inviteId);
            validateInviteAccessible(invite, schoolId);
            if (userId.equals(invite.getHostId())) {
                throw new BusinessException(40025, "发起人不可退出");
            }

            InviteMember member = findMember(inviteId, userId);
            if (member == null || !"active".equalsIgnoreCase(member.getStatus())) {
                throw new BusinessException(40901, "尚未加入该邀请");
            }

            member.setStatus("left");
            member.setLeftAt(LocalDateTime.now());
            int updatedMember = inviteMemberMapper.updateById(member);
            if (updatedMember <= 0) {
                throw new BusinessException(50001, "更新成员状态失败，请稍后重试");
            }

            int maxPlayers = invite.getMaxPlayers() == null ? 0 : invite.getMaxPlayers();
            updateInviteLeaveCount(inviteId, maxPlayers);
            evictInviteCaches();

            Invite updated = inviteMapper.selectById(inviteId);
            return buildInviteVOs(Collections.singletonList(updated), userId)
                    .stream()
                    .findFirst()
                    .orElseGet(InviteListVO::new);
        } finally {
            releaseJoinLock(lockKey);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public InviteListVO cancelInvite(Long inviteId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        if (inviteId == null || inviteId <= 0) {
            throw new BusinessException(40401, "邀请不存在");
        }
        Long schoolId = resolveSchoolId(userId);

        String lockKey = buildJoinLockKey(inviteId);
        if (!acquireJoinLock(lockKey)) {
            throw new BusinessException(40901, "操作过于频繁，请稍后重试");
        }

        try {
            Invite invite = inviteMapper.selectById(inviteId);
            validateInviteCancelable(invite, schoolId, userId);

            LocalDateTime now = LocalDateTime.now();
            inviteMemberMapper.update(
                    null,
                    new UpdateWrapper<InviteMember>()
                            .eq("invite_id", inviteId)
                            .eq("status", "active")
                            .set("status", "left")
                            .set("left_at", now)
            );

            UpdateWrapper<Invite> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", inviteId)
                    .ne("status", "canceled")
                    .ne("status", "finished")
                    .set("status", "canceled")
                    .set("joined_count", 0);
            int updatedRows = inviteMapper.update(null, updateWrapper);
            if (updatedRows <= 0) {
                throw new BusinessException(40901, "操作冲突，请刷新后重试");
            }

            evictInviteCaches();

            Invite updatedInvite = inviteMapper.selectById(inviteId);
            return buildInviteVOs(Collections.singletonList(updatedInvite), userId)
                    .stream()
                    .findFirst()
                    .orElseGet(InviteListVO::new);
        } finally {
            releaseJoinLock(lockKey);
        }
    }


    private Category resolveCategoryById(Long categoryId) {
        Integer normalizedId = normalizeCategoryId(categoryId);
        if (normalizedId == null) {
            throw new BusinessException(40004, "运动分类不能为空");
        }
        Category category = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getId, normalizedId)
                        .eq(Category::getStatus, 1)
        );
        if (category == null) {
            throw new BusinessException(40004, "运动分类不存在或已停用");
        }
        return category;
    }

    private Integer resolveCategoryId(Long categoryId) {
        Integer normalizedId = normalizeCategoryId(categoryId);
        if (normalizedId == null) {
            return null;
        }
        Category category = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getId, normalizedId)
                        .eq(Category::getStatus, 1)
        );
        return category == null ? null : category.getId();
    }

    private Integer normalizeCategoryId(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        if (categoryId <= 0 || categoryId > Integer.MAX_VALUE) {
            return null;
        }
        return categoryId.intValue();
    }

    private void validateActivityDateTime(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            throw new BusinessException(40004, "活动日期和时间不能为空");
        }
        LocalDateTime activityDateTime = date.atTime(time);
        if (activityDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(40004, "活动时间不能早于当前时间");
        }
    }

    private void validateMaxPlayers(Integer maxPlayers) {
        if (maxPlayers == null || maxPlayers < 2 || maxPlayers > 50) {
            throw new BusinessException(40004, "最大人数需在2-50之间");
        }
    }

    private Invite buildInviteEntity(CreateInviteDTO request, Long userId, Long schoolId, Category category) {
        Invite invite = new Invite();
        invite.setHostId(userId);
        invite.setSchoolId(schoolId);
        invite.setCategoryId(category.getId());
        invite.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : null);
        invite.setDescription(request.getDescription());
        invite.setActivityDate(request.getActivityDate());
        invite.setActivityTime(request.getActivityTime());
        invite.setLocation(request.getLocation());
        invite.setMaxPlayers(request.getMaxPlayers());
        invite.setJoinedCount(1);
        invite.setStatus("open");
        invite.setShareToken(resolveShareToken(request.getShareToken()));
        return invite;
    }

    private void saveHostMember(Long inviteId, Long userId) {
        InviteMember member = new InviteMember();
        member.setInviteId(inviteId);
        member.setUserId(userId);
        member.setRole("host");
        member.setStatus("active");
        member.setJoinedAt(LocalDateTime.now());
        int rows = inviteMemberMapper.insert(member);
        if (rows <= 0) {
            throw new BusinessException(50001, "创建邀请成员失败，请稍后重试");
        }
    }

    private String resolveShareToken(String provided) {
        if (StringUtils.hasText(provided)) {
            return provided.trim();
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private boolean acquireCreateLock(String key) {
        try {
            Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", CREATE_LOCK_TTL);
            return Boolean.TRUE.equals(locked);
        } catch (Exception e) {
            log.warn("尝试获取创建邀请锁失败，key={}", key, e);
            return true;
        }
    }

    private void releaseCreateLock(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("释放创建邀请锁失败，key={}", key, e);
        }
    }

    private String buildCreateCountKey(Long userId) {
        return "cnt:invite:create:" + userId + ":" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private long incrementCreateCount(String key) {
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                Duration ttl = Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay());
                stringRedisTemplate.expire(key, ttl);
            }
            return count == null ? 0L : count;
        } catch (Exception e) {
            log.warn("创建邀请计数失败，降级为不限制，key={}", key, e);
            return 0L;
        }
    }

    private void validateInviteJoinable(Invite invite, Long schoolId) {
        if (invite == null || invite.getSchoolId() == null || !invite.getSchoolId().equals(schoolId)) {
            throw new BusinessException(40401, "邀请不存在");
        }
        if (!"open".equalsIgnoreCase(invite.getStatus())) {
            throw new BusinessException(40021, "邀请已关闭或不可加入");
        }
        if (isExpired(invite, LocalDateTime.now())) {
            throw new BusinessException(40023, "邀请已过期");
        }
    }


    private void validateInviteAccessible(Invite invite, Long schoolId) {
        if (invite == null || invite.getSchoolId() == null || !invite.getSchoolId().equals(schoolId)) {
            throw new BusinessException(40401, "邀请不存在");
        }
        if ("canceled".equalsIgnoreCase(invite.getStatus()) || "finished".equalsIgnoreCase(invite.getStatus())) {
            throw new BusinessException(40021, "邀请已关闭/取消/结束");
        }
    }

    private void validateInviteCancelable(Invite invite, Long schoolId, Long userId) {
        if (invite == null || invite.getSchoolId() == null || !invite.getSchoolId().equals(schoolId)) {
            throw new BusinessException(40401, "邀请不存在");
        }
        if (!userId.equals(invite.getHostId())) {
            throw new BusinessException(40301, "仅发起人可以操作");
        }
        if ("canceled".equalsIgnoreCase(invite.getStatus())) {
            throw new BusinessException(40901, "邀请已取消");
        }
        if ("finished".equalsIgnoreCase(invite.getStatus())) {
            throw new BusinessException(40021, "邀请已结束或关闭，无法取消");
        }
    }

    private void validateInviteReadable(Invite invite, Long schoolId) {
        if (invite == null || invite.getSchoolId() == null) {
            throw new BusinessException(40401, "邀请不存在");
        }
        if (schoolId != null && !invite.getSchoolId().equals(schoolId)) {
            throw new BusinessException(40401, "邀请不存在");
        }
        String status = invite.getStatus() == null ? "" : invite.getStatus().toLowerCase();
        if (!Arrays.asList("open", "full", "finished").contains(status)) {
            throw new BusinessException(40401, "邀请不存在");
        }
        boolean expired = isExpired(invite, LocalDateTime.now());
        if (expired && !"finished".equalsIgnoreCase(status)) {
            throw new BusinessException(40401, "邀请不存在");
        }
    }


    private InviteMember findMember(Long inviteId, Long userId) {
        return inviteMemberMapper.selectOne(
                new LambdaQueryWrapper<InviteMember>()
                        .eq(InviteMember::getInviteId, inviteId)
                        .eq(InviteMember::getUserId, userId)
                        .last("limit 1")
        );
    }

    private void updateInviteJoinCount(Long inviteId, int maxPlayers) {
        UpdateWrapper<Invite> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", inviteId)
                .eq("status", "open")
                .apply("IFNULL(joined_count,0) < {0}", maxPlayers);
        String setSql = "joined_count = IFNULL(joined_count,0) + 1";
        if (maxPlayers > 0) {
            setSql += ", status = CASE WHEN IFNULL(joined_count,0) + 1 >= IFNULL(max_players,0) THEN 'full' ELSE status END";
        }
        wrapper.setSql(setSql);
        int updated = inviteMapper.update(null, wrapper);
        if (updated <= 0) {
            throw new BusinessException(40901, "名额已满，请刷新后再试");
        }
    }


    private void updateInviteLeaveCount(Long inviteId, int maxPlayers) {
        UpdateWrapper<Invite> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", inviteId)
                .apply("IFNULL(joined_count,0) > 0")
                .in("status", Arrays.asList("open", "full"));

        String setSql = "joined_count = CASE WHEN IFNULL(joined_count,0) > 0 THEN IFNULL(joined_count,0) - 1 ELSE 0 END";
        if (maxPlayers > 0) {
            setSql += ", status = CASE WHEN status = 'full' AND IFNULL(joined_count,0) - 1 < IFNULL(max_players,0) THEN 'open' ELSE status END";
        }
        wrapper.setSql(setSql);
        int updated = inviteMapper.update(null, wrapper);
        if (updated <= 0) {
            throw new BusinessException(40901, "操作冲突，请刷新后重试");
        }
    }



    private String buildJoinLockKey(Long inviteId) {
        return "lock:invite:join:" + inviteId;
    }

    private boolean acquireJoinLock(String key) {
        try {
            Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", JOIN_LOCK_TTL);
            return Boolean.TRUE.equals(locked);
        } catch (Exception e) {
            log.warn("尝试获取加入邀请锁失败，key={}", key, e);
            return false;
        }
    }

    private void releaseJoinLock(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("释放加入邀请锁失败，key={}", key, e);
        }
    }


    private Long resolveSchoolId(Long userId) {
        Long schoolId = UserContext.getSchoolId();
        if (schoolId != null) {
            return schoolId;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        if (user.getSchoolId() == null) {
            throw new BusinessException(40004, "学校信息缺失");
        }
        return user.getSchoolId();
    }

    private Set<String> parseStatuses(String statusParam) {
        if (!StringUtils.hasText(statusParam)) {
            return new HashSet<>(Collections.singletonList("open"));
        }
        Set<String> statuses = new HashSet<>();
        for (String s : statusParam.split(",")) {
            if (StringUtils.hasText(s)) {
                statuses.add(s.trim().toLowerCase());
            }
        }
        if (statuses.isEmpty()) {
            statuses.add("open");
        }
        return statuses;
    }

    private Set<String> parseMineStatuses(String statusParam) {
        if (!StringUtils.hasText(statusParam)) {
            return new HashSet<>(Collections.singletonList("all"));
        }
        Set<String> statuses = new HashSet<>();
        for (String s : statusParam.split(",")) {
            if (StringUtils.hasText(s)) {
                statuses.add(s.trim().toLowerCase());
            }
        }
        if (statuses.isEmpty()) {
            statuses.add("all");
        }
        return statuses;
    }

    private String normalizeView(String view) {
        return StringUtils.hasText(view) ? view.trim().toLowerCase() : "all";
    }

    private void applyViewFilter(LambdaQueryWrapper<Invite> wrapper, String view, Long userId) {
        String normalizedView = normalizeView(view);
        String memberSql = String.format("select invite_id from invite_members where user_id = %d and status = 'active' and role <> 'host'", userId);
        switch (normalizedView) {
            case "host" -> wrapper.eq(Invite::getHostId, userId);
            case "joined" -> wrapper.inSql(Invite::getId, memberSql).ne(Invite::getHostId, userId);
            default -> wrapper.and(w -> w.eq(Invite::getHostId, userId)
                    .or(o -> o.inSql(Invite::getId, memberSql)));
        }
    }

    private void applyStatusFilterForMine(LambdaQueryWrapper<Invite> wrapper, Set<String> statuses) {
        if (CollectionUtils.isEmpty(statuses) || statuses.contains("all")) {
            return;
        }
        final LocalDate today = LocalDate.now();
        final LocalTime nowTime = LocalTime.now();
        boolean includeExpired = statuses.contains("expired");
        boolean includeFull = statuses.contains("full");
        Set<String> dbStatuses = statuses.stream()
                .filter(s -> !"expired".equals(s) && !"full".equals(s))
                .collect(Collectors.toSet());

        wrapper.and(w -> {
            boolean added = false;
            if (includeExpired) {
                w.lt(Invite::getActivityDate, today)
                        .or(o -> o.eq(Invite::getActivityDate, today).lt(Invite::getActivityTime, nowTime));
                added = true;
            }
            if (includeFull) {
                if (added) {
                    w.or();
                }
                w.apply("(status = 'full' OR (status = 'open' AND IFNULL(joined_count,0) >= IFNULL(max_players,0)))");
                added = true;
            }
            if (!CollectionUtils.isEmpty(dbStatuses)) {
                if (added) {
                    w.or();
                }
                w.in(Invite::getStatus, dbStatuses);
            }
        });
    }

    private void applyMineOrder(LambdaQueryWrapper<Invite> wrapper, String view) {
        String normalizedView = normalizeView(view);
        if ("joined".equals(normalizedView)) {
            wrapper.orderByDesc(Invite::getActivityDate)
                    .orderByDesc(Invite::getActivityTime)
                    .orderByDesc(Invite::getCreatedAt);
            return;
        }
        wrapper.orderByDesc(Invite::getCreatedAt);
    }

    private void applyStatusFilter(LambdaQueryWrapper<Invite> wrapper, Set<String> statuses) {
        if (CollectionUtils.isEmpty(statuses) || statuses.contains("all")) {
            return;
        }
        Set<String> realStatus = statuses.stream()
                .filter(s -> !"expired".equals(s))
                .collect(Collectors.toSet());
        if (!realStatus.isEmpty()) {
            wrapper.in(Invite::getStatus, realStatus);
        }
    }

    private void applyExpireFilter(LambdaQueryWrapper<Invite> wrapper, boolean excludeExpired) {
        if (!excludeExpired) {
            return;
        }
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        wrapper.and(w -> w.gt(Invite::getActivityDate, today)
                .or(o -> o.eq(Invite::getActivityDate, today)
                        .ge(Invite::getActivityTime, nowTime)));
    }

    private void applyOrder(LambdaQueryWrapper<Invite> wrapper, String orderParam) {
        String order = StringUtils.hasText(orderParam) ? orderParam.trim().toLowerCase() : "created_at desc";
        switch (order) {
            case "activity_date asc" -> wrapper.orderByAsc(Invite::getActivityDate).orderByAsc(Invite::getActivityTime);
            case "activity_date desc" -> wrapper.orderByDesc(Invite::getActivityDate).orderByDesc(Invite::getActivityTime);
            case "activity_time asc" -> wrapper.orderByAsc(Invite::getActivityTime);
            case "activity_time desc" -> wrapper.orderByDesc(Invite::getActivityTime);
            default -> wrapper.orderByDesc(Invite::getCreatedAt);
        }
    }

    private List<InviteListVO> buildInviteVOs(List<Invite> invites, Long currentUserId) {
        if (CollectionUtils.isEmpty(invites)) {
            return Collections.emptyList();
        }
        Set<Long> hostIds = invites.stream()
                .map(Invite::getHostId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> hostMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(hostIds)) {
            List<User> users = userMapper.selectBatchIds(hostIds);
            hostMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        List<Long> inviteIds = invites.stream().map(Invite::getId).collect(Collectors.toList());
        Set<Long> joinedIds = resolveJoinedInviteIds(inviteIds, currentUserId);

        LocalDateTime now = LocalDateTime.now();
        Map<Long, User> finalHostMap = hostMap;
        return invites.stream().map(invite -> {
            InviteListVO vo = new InviteListVO();
            vo.setId(invite.getId());
            vo.setHostId(invite.getHostId());
            vo.setSchoolId(invite.getSchoolId());
            vo.setCategoryId(invite.getCategoryId() == null ? null : invite.getCategoryId().longValue());
            vo.setTitle(invite.getTitle());
            vo.setDescription(invite.getDescription());
            vo.setActivityDate(invite.getActivityDate());
            vo.setActivityTime(invite.getActivityTime());
            vo.setLocation(invite.getLocation());
            vo.setMaxPlayers(invite.getMaxPlayers());
            vo.setJoinedCount(invite.getJoinedCount() == null ? 0 : invite.getJoinedCount());
            vo.setShareToken(invite.getShareToken());
            vo.setCreatedAt(invite.getCreatedAt());

            User host = finalHostMap.get(invite.getHostId());
            if (host != null) {
                vo.setHostName(host.getNickname());
                vo.setHostAvatar(host.getAvatar());
            }

            boolean joined = currentUserId != null
                    && (joinedIds.contains(invite.getId()) || currentUserId.equals(invite.getHostId()));
            vo.setIsJoined(joined);

            String statusForView = resolveStatusForView(invite, now);
            vo.setStatus(statusForView);
            return vo;
        }).collect(Collectors.toList());
    }

    private Set<Long> resolveJoinedInviteIds(List<Long> inviteIds, Long userId) {
        if (CollectionUtils.isEmpty(inviteIds) || userId == null) {
            return Collections.emptySet();
        }
        List<InviteMember> members = inviteMemberMapper.selectList(
                new LambdaQueryWrapper<InviteMember>()
                        .in(InviteMember::getInviteId, inviteIds)
                        .eq(InviteMember::getUserId, userId)
                        .eq(InviteMember::getStatus, "active")
        );
        return members.stream()
                .map(InviteMember::getInviteId)
                .collect(Collectors.toSet());
    }

    private List<InviteMemberBriefVO> buildMemberBriefs(List<InviteMember> members) {
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        return members.stream().map(member -> {
            InviteMemberBriefVO vo = new InviteMemberBriefVO();
            vo.setUserId(member.getUserId());
            vo.setRole(member.getRole());
            vo.setStatus(member.getStatus());
            vo.setJoinedAt(member.getJoinedAt());
            vo.setLeftAt(member.getLeftAt());
            return vo;
        }).collect(Collectors.toList());
    }

    private List<InviteMemberDetailVO> buildMemberDetails(List<InviteMember> members) {
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        Set<Long> userIds = members.stream()
                .map(InviteMember::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(userIds)) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        Map<Long, User> finalUserMap = userMap;
        return members.stream().map(member -> {
            InviteMemberDetailVO vo = new InviteMemberDetailVO();
            vo.setUserId(member.getUserId());
            vo.setRole(member.getRole());
            vo.setStatus(member.getStatus());
            vo.setJoinedAt(member.getJoinedAt());
            vo.setLeftAt(member.getLeftAt());

            User user = finalUserMap.get(member.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private String resolveStatusForView(Invite invite, LocalDateTime now) {
        boolean expired = isExpired(invite, now);
        if (expired && !"finished".equalsIgnoreCase(invite.getStatus())) {
            return "expired";
        }
        String status = invite.getStatus();
        int joined = invite.getJoinedCount() == null ? 0 : invite.getJoinedCount();
        int maxPlayers = invite.getMaxPlayers() == null ? 0 : invite.getMaxPlayers();
        if ("open".equalsIgnoreCase(status) && maxPlayers > 0 && joined >= maxPlayers) {
            return "full";
        }
        return status;
    }

    private boolean isExpired(Invite invite, LocalDateTime now) {
        if (invite.getActivityDate() == null) {
            return false;
        }
        LocalTime time = invite.getActivityTime() == null ? LocalTime.MAX : invite.getActivityTime();
        LocalDateTime activityDateTime = invite.getActivityDate().atTime(time);
        return activityDateTime.isBefore(now);
    }

    private String buildDetailCacheKey(Long inviteId, Long userId) {
        String userPart = userId == null ? "anon" : userId.toString();
        return String.format("invite:detail:%d:%s", inviteId, userPart);
    }

    private InviteDetailVO readDetailFromCache(String cacheKey) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (!StringUtils.hasText(cached)) {
                return null;
            }
            return objectMapper.readValue(cached, InviteDetailVO.class);
        } catch (Exception e) {
            log.warn("读取邀请详情缓存失败，key={}", cacheKey, e);
            return null;
        }
    }

    private void writeDetailToCache(String cacheKey, InviteDetailVO value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(45));
        } catch (Exception e) {
            log.warn("写入邀请详情缓存失败，key={}", cacheKey, e);
        }
    }

    private String buildCacheKey(Long schoolId, Long categoryId, Set<String> status, boolean excludeExpired, int current, int size, String order, Long userId) {
        String statusPart = CollectionUtils.isEmpty(status)
                ? "open"
                : status.stream().sorted().collect(Collectors.joining("-"));
        String categoryPart = categoryId == null ? "all" : categoryId.toString();
        String orderPart = StringUtils.hasText(order) ? order.trim().toLowerCase() : "created_at desc";
        String userPart = userId == null ? "anon" : userId.toString();
        return String.format("invite:list:%s:%s:%s:%s:%d:%d:%s:%s",
                schoolId, categoryPart, statusPart, excludeExpired, current, size, orderPart, userPart);
    }

    private PageResult<InviteListVO> readFromCache(String cacheKey) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (!StringUtils.hasText(cached)) {
                return null;
            }
            return objectMapper.readValue(cached, new TypeReference<PageResult<InviteListVO>>() {
            });
        } catch (Exception e) {
            log.warn("读取邀请列表缓存失败，key={}", cacheKey, e);
            return null;
        }
    }

    private void writeToCache(String cacheKey, PageResult<InviteListVO> value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(45));
        } catch (Exception e) {
            log.warn("写入邀请列表缓存失败，key={}", cacheKey, e);
        }
    }

    private void evictInviteCaches() {
        try {
            Set<String> listKeys = stringRedisTemplate.keys("invite:list:*");
            if (!CollectionUtils.isEmpty(listKeys)) {
                stringRedisTemplate.delete(listKeys);
            }
            Set<String> detailKeys = stringRedisTemplate.keys("invite:detail:*");
            if (!CollectionUtils.isEmpty(detailKeys)) {
                stringRedisTemplate.delete(detailKeys);
            }
            Set<String> mineKeys = stringRedisTemplate.keys("invite:mine:*");
            if (!CollectionUtils.isEmpty(mineKeys)) {
                stringRedisTemplate.delete(mineKeys);
            }
        } catch (Exception e) {
            log.warn("清理邀请相关缓存失败", e);
        }
    }
}

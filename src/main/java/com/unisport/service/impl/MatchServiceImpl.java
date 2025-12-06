package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unisport.dto.MatchQueryDTO;
import com.unisport.entity.Category;
import com.unisport.entity.Match;
import com.unisport.entity.MatchEvent;
import com.unisport.entity.TeamMember;
import com.unisport.mapper.CategoryMapper;
import com.unisport.mapper.MatchEventMapper;
import com.unisport.mapper.MatchMapper;
import com.unisport.mapper.TeamMemberMapper;
import com.unisport.service.MatchService;
import com.unisport.vo.MatchDetailVO;
import com.unisport.vo.MatchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 比赛服务实现类
 *
 * @author UniSport Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchMapper matchMapper;
    private final CategoryMapper categoryMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final MatchEventMapper matchEventMapper;

    @Override
    public Page<MatchVO> getMatchList(MatchQueryDTO queryDTO) {
        log.info("查询比赛列表，参数：{}", queryDTO);

        // 构建分页对象
        Page<Match> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 构建查询条件
        LambdaQueryWrapper<Match> queryWrapper = new LambdaQueryWrapper<>();

        // 按分类筛选
        if (!"all".equals(queryDTO.getCategoryCode())) {
            Category category = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>().eq(Category::getCode, queryDTO.getCategoryCode())
            );
            if (category != null) {
                queryWrapper.eq(Match::getCategoryId, category.getId());
            }
        }

        // 按状态筛选
        if (!"all".equals(queryDTO.getStatus())) {
            queryWrapper.eq(Match::getStatus, queryDTO.getStatus());
        }

        // 按比赛时间倒序
        queryWrapper.orderByDesc(Match::getMatchTime);

        // 执行分页查询
        Page<Match> matchPage = matchMapper.selectPage(page, queryWrapper);

        // 获取所有分类信息（用于填充分类名称）
        List<Category> categories = categoryMapper.selectList(null);
        Map<Integer, Category> categoryMap = categories.stream()
            .collect(Collectors.toMap(Category::getId, c -> c));

        // 转换为VO
        Page<MatchVO> voPage = new Page<>(matchPage.getCurrent(), matchPage.getSize(), matchPage.getTotal());
        List<MatchVO> voList = matchPage.getRecords().stream().map(match -> {
            MatchVO vo = new MatchVO();
            BeanUtils.copyProperties(match, vo);
            
            // 填充分类信息
            Category category = categoryMap.get(match.getCategoryId());
            if (category != null) {
                vo.setCategoryCode(category.getCode());
                vo.setCategoryName(category.getName());
            }
            
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);

        log.info("查询到 {} 条比赛记录", voPage.getTotal());
        return voPage;
    }

    @Override
    public MatchDetailVO getMatchDetail(Long id) {
        log.info("查询比赛详情，比赛ID：{}", id);

        try {
            // 查询比赛基本信息
            Match match = matchMapper.selectById(id);
            if (match == null) {
                log.warn("比赛不存在，ID：{}", id);
                return null;
            }

            // 构建返回对象
            MatchDetailVO vo = new MatchDetailVO();
            BeanUtils.copyProperties(match, vo);

            // 填充分类信息
            Category category = categoryMapper.selectById(match.getCategoryId());
            if (category != null) {
                vo.setCategoryCode(category.getCode());
                vo.setCategoryName(category.getName());
            }

            // 查询A队球员阵容
            if (match.getTeamAId() != null) {
                List<TeamMember> playersA = teamMemberMapper.selectList(
                    new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, match.getTeamAId())
                        .orderByAsc(TeamMember::getJerseyNumber)
                );
                List<MatchDetailVO.PlayerVO> playerVOsA = playersA.stream().map(player -> {
                    MatchDetailVO.PlayerVO playerVO = new MatchDetailVO.PlayerVO();
                    playerVO.setId(player.getId());
                    playerVO.setPlayerName(player.getPlayerName());
                    playerVO.setJerseyNumber(player.getJerseyNumber());
                    playerVO.setPosition(player.getPosition());
                    return playerVO;
                }).collect(Collectors.toList());
                vo.setPlayersA(playerVOsA);
            } else {
                vo.setPlayersA(new java.util.ArrayList<>());
            }

            // 查询B队球员阵容
            if (match.getTeamBId() != null) {
                List<TeamMember> playersB = teamMemberMapper.selectList(
                    new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, match.getTeamBId())
                        .orderByAsc(TeamMember::getJerseyNumber)
                );
                List<MatchDetailVO.PlayerVO> playerVOsB = playersB.stream().map(player -> {
                    MatchDetailVO.PlayerVO playerVO = new MatchDetailVO.PlayerVO();
                    playerVO.setId(player.getId());
                    playerVO.setPlayerName(player.getPlayerName());
                    playerVO.setJerseyNumber(player.getJerseyNumber());
                    playerVO.setPosition(player.getPosition());
                    return playerVO;
                }).collect(Collectors.toList());
                vo.setPlayersB(playerVOsB);
            } else {
                vo.setPlayersB(new java.util.ArrayList<>());
            }

            // 查询比赛事件时间轴（按时间正序）
            List<MatchEvent> events = matchEventMapper.selectList(
                new LambdaQueryWrapper<MatchEvent>()
                    .eq(MatchEvent::getMatchId, id)
                    .orderByAsc(MatchEvent::getMinute)
            );
            List<MatchDetailVO.EventVO> eventVOs = events.stream().map(event -> {
                MatchDetailVO.EventVO eventVO = new MatchDetailVO.EventVO();
                eventVO.setId(event.getId());
                eventVO.setEventType(event.getEventType());
                eventVO.setMinute(event.getMinute());
                eventVO.setDescription(event.getDescription());
                return eventVO;
            }).collect(Collectors.toList());
            vo.setEvents(eventVOs);

            log.info("比赛详情查询成功，A队球员数：{}，B队球员数：{}，事件数：{}", 
                vo.getPlayersA() != null ? vo.getPlayersA().size() : 0,
                vo.getPlayersB() != null ? vo.getPlayersB().size() : 0,
                vo.getEvents().size());
            
            return vo;
        } catch (Exception e) {
            log.error("查询比赛详情失败，比赛ID：{}", id, e);
            throw new RuntimeException("查询比赛详情失败", e);
        }
    }
}

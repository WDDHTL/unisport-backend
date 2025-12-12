package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unisport.common.BusinessException;
import com.unisport.common.UserContext;
import com.unisport.dto.MatchQueryDTO;
import com.unisport.entity.*;
import com.unisport.mapper.*;
import com.unisport.service.MatchService;
import com.unisport.vo.MatchDetailVO;
import com.unisport.vo.MatchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final UserMapper userMapper;
    private final LeagueMapper leagueMapper;

    @Override
    public List<MatchVO> getMatchList(MatchQueryDTO queryDTO) {
        log.info("查询比赛列表，参数：{}", queryDTO);

        // 获取用户信息
        Long userId = UserContext.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userId));
        // TODO long->int
        queryDTO.setSchoolId(user.getSchoolId().intValue());

        // 构建查询条件
        LambdaQueryWrapper<Match> queryWrapper = new LambdaQueryWrapper<>();

        // 构建查询参数
        if (queryDTO.getLeagueId() != 0){
            queryWrapper.eq(Match::getLeagueId, queryDTO.getLeagueId());
        }
        if (queryDTO.getCategoryId() == null){
            new BusinessException("获取比赛列表分类ID不能为空");
        }
        queryWrapper.eq(Match::getCategoryId, queryDTO.getCategoryId());
        if (queryDTO.getSchoolId() == null){
            new BusinessException("获取比赛列表学校ID不能为空");
        }
        queryWrapper.eq(Match::getSchoolId, queryDTO.getSchoolId());

        // 按状态筛选
        if (!"all".equals(queryDTO.getStatus())) {
            queryWrapper.eq(Match::getStatus, queryDTO.getStatus());
        }


        // 按比赛时间倒序
        queryWrapper.orderByDesc(Match::getMatchTime);

        // 执行查询--转换VO
        List<Match> matches = matchMapper.selectList(queryWrapper);
        Category category = categoryMapper.selectById(queryDTO.getCategoryId());
        List<MatchVO> voList = matches.stream().map(match -> {
            MatchVO vo = new MatchVO();
            BeanUtils.copyProperties(match, vo);
            vo.setCategoryCode(category.getCode());
            vo.setCategoryName(category.getName());

            return vo;
        }).collect(Collectors.toList());

        log.info("查询到 {} 条数据", voList.size());
        return voList;
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

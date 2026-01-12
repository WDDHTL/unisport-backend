package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.common.UserContext;
import com.unisport.entity.*;
import com.unisport.mapper.*;
import com.unisport.service.PlayerService;
import com.unisport.vo.PlayerStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2025/12/8$
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerMapper playerMapper;

    private final CategoryMapper categoryMapper;

    private final LeagueMapper leagueMapper;

    private final TeamMemberMapper teamMemberMapper;

    private final TeamMapper teamMapper;

    private final UserMapper userMapper;

    @Override
    public List<PlayerStatsVO> getStats(Integer categoryId, Integer leagueId, Integer year) {
        if (year == null){
            year = LocalDate.now().getYear();
        }

        Long userId = UserContext.getUserId();
        Long schoolId = UserContext.getSchoolId();
        if (schoolId == null && userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                schoolId = user.getSchoolId();
            }
        }
        log.info("查询球员榜，分类ID：{}，联赛ID：{}，年份：{}，学校：{}", categoryId, leagueId, year, schoolId);

        try {
            if (categoryId == null) {
                log.warn("运动ID不存在或已禁用：{}", categoryId);
                return new java.util.ArrayList<>();
            }

//            // 查询该分类在指定年份、指定学校的联赛
//            League league = leagueMapper.selectOne(
//                    new LambdaQueryWrapper<League>()
//                            .eq(League::getCategoryId, categoryId)
//                            .eq(League::getYear, year)
//                            .eq(League::getSchoolId, schoolId)
//                            .orderByDesc(League::getCreatedAt)
//                            .last("LIMIT 1")
//            );

            if (leagueId == null) {
                log.warn("未找到对应的联赛，分类ID：{}，年份：{}", categoryId, year);
                return new java.util.ArrayList<>();
            }

            List<PlayerStats> playerStats = playerMapper.selectList(
                    new LambdaQueryWrapper<PlayerStats>()
                            .eq(PlayerStats::getLeagueId, leagueId)
                            .orderByDesc(PlayerStats::getStatValue)
            );

            List<PlayerStatsVO> voList = playerStats.stream().map(player -> {
                PlayerStatsVO vo = new PlayerStatsVO();
                BeanUtils.copyProperties(player, vo);
                player.getTeamMemberId();
                TeamMember teamMember = teamMemberMapper.selectOne(
                        new LambdaQueryWrapper<TeamMember>()
                                .eq(TeamMember::getId, player.getTeamMemberId())
                );
                vo.setPlayerName(teamMember.getPlayerName());
                vo.setTeamName(teamMapper.selectOne(
                        new LambdaQueryWrapper<Team>()
                                .eq(Team::getId, teamMember.getTeamId())
                ).getName());
                vo.setGoals(player.getStatValue());
                vo.setRank(player.getPlayerRank()); // Changed from setRank(player.getRank());
                return vo;
            }).collect(Collectors.toList());

            log.info("查询到 {} 条球员榜记录", voList.size());
            return voList;
        }catch (Exception e){
            log.error("查询球员榜失败，分类ID：{}，年份：{}", categoryId, year, e);
            throw new RuntimeException("查询球员榜失败", e);
        }
    }
}

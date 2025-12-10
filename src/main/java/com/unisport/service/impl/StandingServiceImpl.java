package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.common.UserContext;
import com.unisport.entity.Category;
import com.unisport.entity.League;
import com.unisport.entity.Standing;
import com.unisport.entity.User;
import com.unisport.mapper.CategoryMapper;
import com.unisport.mapper.LeagueMapper;
import com.unisport.mapper.StandingMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.service.StandingService;
import com.unisport.vo.StandingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分榜服务实现类
 *
 * @author UniSport Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StandingServiceImpl implements StandingService {

    private final StandingMapper standingMapper;
    private final CategoryMapper categoryMapper;
    private final LeagueMapper leagueMapper;
    private final UserMapper userMapper;

    @Override
    public List<StandingVO> getStandings(String categoryCode, Integer year) {
        log.info("查询积分榜，分类：{}，年份：{}", categoryCode, year);

        // 如果未指定年份，使用当前年份
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        // 获取用户学校
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        Long schoolId = user.getSchoolId();

        try {
            // 根据分类代码查询分类ID
            Category category = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                    .eq(Category::getCode, categoryCode)
                    .eq(Category::getStatus, 1)
            );

            if (category == null) {
                log.warn("运动分类不存在或已禁用：{}", categoryCode);
                return new java.util.ArrayList<>();
            }

            // 查询该分类在指定年份、指定学校的联赛
            League league = leagueMapper.selectOne(
                new LambdaQueryWrapper<League>()
                    .eq(League::getCategoryId, category.getId())
                    .eq(League::getYear, year)
                    .eq(League::getSchoolId, schoolId)
                    .orderByDesc(League::getCreatedAt)
                    .last("LIMIT 1")
            );

            if (league == null) {
                log.warn("未找到对应的联赛，分类：{}，年份：{}", categoryCode, year);
                return new java.util.ArrayList<>();
            }

            // 根据联赛ID查询积分榜，按排名升序排列
            List<Standing> standings = standingMapper.selectList(
                new LambdaQueryWrapper<Standing>()
                    .eq(Standing::getLeagueId, league.getId())
                    .orderByAsc(Standing::getRank)
            );

            // 转换为VO
            List<StandingVO> voList = standings.stream().map(standing -> {
                StandingVO vo = new StandingVO();
                BeanUtils.copyProperties(standing, vo);
                return vo;
            }).collect(Collectors.toList());

            log.info("查询到 {} 条积分榜记录", voList.size());
            return voList;

        } catch (Exception e) {
            log.error("查询积分榜失败，分类：{}，年份：{}", categoryCode, year, e);
            throw new RuntimeException("查询积分榜失败", e);
        }
    }
}

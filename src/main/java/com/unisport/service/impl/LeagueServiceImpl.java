package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.unisport.common.BusinessException;
import com.unisport.common.UserContext;
import com.unisport.entity.League;
import com.unisport.entity.Match;
import com.unisport.entity.User;
import com.unisport.mapper.LeagueMapper;
import com.unisport.mapper.SchoolMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.service.LeagueService;
import com.unisport.vo.LeagueVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2025/12/12$
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeagueServiceImpl implements LeagueService {

    private final UserMapper userMapper;

    private final LeagueMapper leagueMapper;

    @Override
    public List<LeagueVO> getLeagueList(Long categoryId, Long schoolId) {
        log.info("请求查询联赛列表，分类ID：{}", categoryId);

        if (categoryId == null){
            new BusinessException("分类无效或分类不存在");
        }

        // 获取用户学校信息
        Long userId = UserContext.getUserId();
        Long currentSchoolId = UserContext.getSchoolId();
        if (currentSchoolId == null) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(40401, "用户不存在");
            }
            currentSchoolId = user.getSchoolId();
        }
        schoolId = currentSchoolId;

        // 构造查询条件
        LambdaQueryWrapper<League> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(League::getCategoryId, categoryId)
                .eq(League::getSchoolId, schoolId);
        List<League> leagues = leagueMapper.selectList(queryWrapper);
        ArrayList<LeagueVO> leagueVOList = new ArrayList<>();
        leagues.forEach(league -> {
            LeagueVO leagueVO = new LeagueVO(league.getId(), league.getName(), league.getCategoryId());
            leagueVOList.add(leagueVO);
        });
        return leagueVOList;
    }
}

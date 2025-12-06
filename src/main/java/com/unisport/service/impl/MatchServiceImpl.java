package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unisport.dto.MatchQueryDTO;
import com.unisport.entity.Category;
import com.unisport.entity.Match;
import com.unisport.mapper.CategoryMapper;
import com.unisport.mapper.MatchMapper;
import com.unisport.service.MatchService;
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
}

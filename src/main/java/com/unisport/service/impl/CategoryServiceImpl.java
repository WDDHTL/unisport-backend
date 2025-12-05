package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.entity.Category;
import com.unisport.mapper.CategoryMapper;
import com.unisport.service.CategoryService;
import com.unisport.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 运动分类服务实现类
 * 实现运动分类查询功能
 *
 * @author UniSport Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 获取运动分类列表
     * 
     * 实现逻辑：
     * 1. 构建查询条件：status=1（只查启用的分类）
     * 2. 按 sort_order 升序排序
     * 3. 转换为VO对象返回
     *
     * @return 运动分类列表
     */
    @Override
    public List<CategoryVO> getCategoryList() {
        log.info("开始查询运动分类列表");

        // 构建查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询启用状态的分类
        queryWrapper.eq(Category::getStatus, 1);
        
        // 按排序字段升序排列
        queryWrapper.orderByAsc(Category::getSortOrder);

        // 查询数据库
        List<Category> categoryList = categoryMapper.selectList(queryWrapper);
        log.info("查询到 {} 个运动分类", categoryList.size());

        // 转换为VO对象
        List<CategoryVO> categoryVOList = categoryList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return categoryVOList;
    }

    /**
     * 将运动分类实体转换为VO对象
     * 
     * @param category 运动分类实体
     * @return 运动分类VO对象
     */
    private CategoryVO convertToVO(Category category) {
        return CategoryVO.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .icon(category.getIcon())
                .sortOrder(category.getSortOrder())
                .build();
    }
}

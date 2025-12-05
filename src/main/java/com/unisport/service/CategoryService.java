package com.unisport.service;

import com.unisport.vo.CategoryVO;

import java.util.List;

/**
 * 运动分类服务接口
 * 提供运动分类查询功能
 *
 * @author UniSport Team
 */
public interface CategoryService {

    /**
     * 获取运动分类列表
     * 
     * 查询规则：
     * 1. 只返回启用状态的分类（status=1）
     * 2. 按 sort_order 升序排序
     * 3. 用于首页导航栏展示分类、发帖时选择分类等场景
     *
     * @return 运动分类列表
     */
    List<CategoryVO> getCategoryList();
}

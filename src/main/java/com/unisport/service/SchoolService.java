package com.unisport.service;

import com.unisport.vo.SchoolVO;

import java.util.List;

/**
 * 学校服务接口
 * 提供学校信息查询功能
 *
 * @author UniSport Team
 */
public interface SchoolService {

    /**
     * 获取学校列表
     * 
     * 查询规则：
     * 1. 只返回启用状态的学校（status=1）
     * 2. 按 sort_order 排序
     * 3. 支持省份和城市筛选（可选）
     *
     * @param province 省份筛选条件（可选）
     * @param city 城市筛选条件（可选）
     * @return 学校信息列表
     */
    List<SchoolVO> getSchoolList(String province, String city);
}

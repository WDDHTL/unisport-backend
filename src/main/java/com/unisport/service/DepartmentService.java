package com.unisport.service;

import com.unisport.vo.DepartmentVO;

import java.util.List;

/**
 * 学院服务接口
 * 提供学院信息查询功能
 *
 * @author UniSport Team
 */
public interface DepartmentService {

    /**
     * 根据学校ID获取学院列表
     * 
     * 查询规则：
     * 1. schoolId 为必填参数
     * 2. 只返回启用状态的学院（status=1）
     * 3. 按 sort_order 排序
     *
     * @param schoolId 学校ID
     * @return 学院信息列表
     */
    List<DepartmentVO> getDepartmentListBySchoolId(Long schoolId);
}

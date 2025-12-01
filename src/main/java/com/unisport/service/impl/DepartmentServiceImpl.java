package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.entity.Department;
import com.unisport.mapper.DepartmentMapper;
import com.unisport.service.DepartmentService;
import com.unisport.vo.DepartmentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学院服务实现类
 * 实现学院信息查询功能
 *
 * @author UniSport Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    /**
     * 根据学校ID获取学院列表
     * 
     * 实现逻辑：
     * 1. 校验schoolId不能为空
     * 2. 构建查询条件：school_id = ? AND status = 1
     * 3. 按 sort_order 排序
     * 4. 转换为VO对象返回
     *
     * @param schoolId 学校ID
     * @return 学院信息列表
     */
    @Override
    public List<DepartmentVO> getDepartmentListBySchoolId(Long schoolId) {
        log.info("开始查询学院列表，学校ID：{}", schoolId);

        // 构建查询条件
        LambdaQueryWrapper<Department> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据学校ID查询
        queryWrapper.eq(Department::getSchoolId, schoolId);
        
        // 只查询启用状态的学院
        queryWrapper.eq(Department::getStatus, 1);
        
        // 按排序字段升序排列
        queryWrapper.orderByAsc(Department::getSortOrder);

        // 查询数据库
        List<Department> departmentList = departmentMapper.selectList(queryWrapper);
        log.info("查询到 {} 个学院", departmentList.size());

        // 转换为VO对象
        List<DepartmentVO> departmentVOList = departmentList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return departmentVOList;
    }

    /**
     * 将学院实体转换为VO对象
     * 
     * @param department 学院实体
     * @return 学院VO对象
     */
    private DepartmentVO convertToVO(Department department) {
        return DepartmentVO.builder()
                .id(department.getId())
                .schoolId(department.getSchoolId())
                .name(department.getName())
                .code(department.getCode())
                .build();
    }
}

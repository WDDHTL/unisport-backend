package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.common.BusinessException;
import com.unisport.entity.Department;
import com.unisport.entity.School;
import com.unisport.entity.User;
import com.unisport.entity.UserEducation;
import com.unisport.mapper.DepartmentMapper;
import com.unisport.mapper.SchoolMapper;
import com.unisport.mapper.UserEducationMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.service.EducationService;
import com.unisport.vo.EducationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 教育经历领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private final UserEducationMapper userEducationMapper;
    private final UserMapper userMapper;
    private final SchoolMapper schoolMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public List<EducationVO> listUserEducations(Long userId) {
        if (userId == null) {
            throw new BusinessException(40004, "用户ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        log.info("查询用户教育经历列表，userId={}", userId);
        List<UserEducation> records = userEducationMapper.selectList(
                new LambdaQueryWrapper<UserEducation>()
                        .eq(UserEducation::getUserId, userId)
                        .orderByDesc(UserEducation::getIsPrimary)
                        .orderByDesc(UserEducation::getCreatedAt)
        );

        boolean hasPrimaryRecord = records.stream().anyMatch(item -> Boolean.TRUE.equals(item.getIsPrimary()));

        List<EducationVO> result = new ArrayList<>();
        // 个人信息中的初始教育，始终返回，若表中无主教育则标记为主
        result.add(buildInitialEducation(user, !hasPrimaryRecord));

        if (CollectionUtils.isEmpty(records)) {
            return result;
        }

        Map<Long, School> schoolMap = resolveSchoolNames(records);
        Map<Long, Department> departmentMap = resolveDepartmentNames(records);

        List<EducationVO> mapped = records.stream()
                .map(item -> {
                    EducationVO vo = new EducationVO();
                    vo.setId(item.getId());
                    vo.setUserId(item.getUserId());
                    vo.setSchoolId(item.getSchoolId());
                    vo.setDepartmentId(item.getDepartmentId());
                    vo.setStudentId(item.getStudentId());
                    vo.setStartDate(item.getStartDate());
                    vo.setEndDate(item.getEndDate());
                    vo.setPrimary(Boolean.TRUE.equals(item.getIsPrimary()));
                    vo.setStatus(item.getStatus());
                    vo.setCreatedAt(item.getCreatedAt());

                    String schoolName = schoolMap.getOrDefault(item.getSchoolId(), new School()).getName();
                    vo.setSchool(schoolName);

                    Department dept = departmentMap.get(item.getDepartmentId());
                    vo.setDepartment(dept != null ? dept.getName() : null);

                    return vo;
                })
                .collect(Collectors.toList());

        result.addAll(mapped);
        return result;
    }

    private Map<Long, School> resolveSchoolNames(List<UserEducation> records) {
        Set<Long> ids = records.stream()
                .map(UserEducation::getSchoolId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(ids)) {
            return Map.of();
        }
        List<School> schools = schoolMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(schools)) {
            return Map.of();
        }
        return schools.stream().collect(Collectors.toMap(School::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, Department> resolveDepartmentNames(List<UserEducation> records) {
        Set<Long> ids = records.stream()
                .map(UserEducation::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(ids)) {
            return Map.of();
        }
        List<Department> departments = departmentMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(departments)) {
            return Map.of();
        }
        return departments.stream().collect(Collectors.toMap(Department::getId, Function.identity(), (a, b) -> a));
    }

    private EducationVO buildInitialEducation(User user, boolean markPrimary) {
        EducationVO vo = new EducationVO();
        vo.setId(null);
        vo.setUserId(user.getId());
        vo.setSchoolId(user.getSchoolId());
        vo.setSchool(user.getSchool());
        vo.setDepartmentId(null);
        vo.setDepartment(user.getDepartment());
        vo.setStudentId(user.getStudentId());
        vo.setStartDate(null);
        vo.setEndDate(null);
        vo.setPrimary(markPrimary);
        vo.setStatus(StringUtils.hasText(user.getStudentId()) ? "verified" : "pending");
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}

package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.common.BusinessException;
import com.unisport.common.UserContext;
import com.unisport.config.JwtProperties;
import com.unisport.dto.AddEducationDTO;
import com.unisport.entity.Department;
import com.unisport.entity.School;
import com.unisport.entity.User;
import com.unisport.entity.UserEducation;
import com.unisport.entity.Student;
import com.unisport.mapper.DepartmentMapper;
import com.unisport.mapper.SchoolMapper;
import com.unisport.mapper.UserEducationMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.mapper.StudentMapper;
import com.unisport.service.EducationService;
import com.unisport.utils.JwtUtil;
import com.unisport.vo.EducationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * 教育经历领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private static final String STATUS_VERIFIED = "verified";
    private static final Pattern YM_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");

    private final UserEducationMapper userEducationMapper;
    private final UserMapper userMapper;
    private final SchoolMapper schoolMapper;
    private final DepartmentMapper departmentMapper;
    private final StudentMapper studentMapper;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EducationVO addEducation(AddEducationDTO addEducationDTO) {
        Long currentUserId = requireLogin();

        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        String studentId = addEducationDTO.getStudentId().trim();
        Long schoolId = addEducationDTO.getSchoolId();
        Long departmentId = addEducationDTO.getDepartmentId();
        String startDate = addEducationDTO.getStartDate().trim();
        String endDate = StringUtils.hasText(addEducationDTO.getEndDate()) ? addEducationDTO.getEndDate().trim() : null;
        validateYearMonth(startDate);
        if (StringUtils.hasText(endDate)) {
            validateYearMonth(endDate);
        }

        log.info("添加教育经历，请求用户ID={}，schoolId={}，departmentId={}", currentUserId, schoolId, departmentId);

        School school = requireSchool(schoolId);
        Department department = requireDepartment(departmentId, schoolId);

        validateStudentIdentity(studentId, schoolId, departmentId);
        ensureEducationNotDuplicated(currentUserId, schoolId, studentId);

        clearPrimaryEducation(currentUserId);

        UserEducation entity = buildEducationEntity(addEducationDTO, currentUserId, studentId, startDate, endDate, true);
        int rows = userEducationMapper.insert(entity);
        if (rows <= 0) {
            throw new BusinessException(50001, "添加失败，请稍后重试");
        }

        UserEducation saved = userEducationMapper.selectById(entity.getId());
        String refreshedToken = refreshLoginToken(user, saved.getSchoolId());
        UserContext.setCurrentUser(currentUserId, saved.getSchoolId());

        return buildEducationVO(saved, school, department, refreshedToken);
    }

    @Override
    public List<EducationVO> listUserEducations(Long userId) {
        if (userId == null) {
            throw new BusinessException(40004, "用户ID不能为空");
        }
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        log.info("查询用户教育经历列表，userId={}", userId);
        List<UserEducation> records = userEducationMapper.selectList(
                new LambdaQueryWrapper<UserEducation>()
                        .eq(UserEducation::getUserId, userId)
                        .orderByDesc(UserEducation::getIsPrimary)
                        .orderByDesc(UserEducation::getCreatedAt)
        );

        if (CollectionUtils.isEmpty(records)) {
            return List.of();
        }

        Map<Long, School> schoolMap = resolveSchoolNames(records);
        Map<Long, Department> departmentMap = resolveDepartmentNames(records);

        return records.stream()
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
    }

    private School requireSchool(Long schoolId) {
        if (schoolId == null) {
            throw new BusinessException(40004, "学校ID不能为空");
        }
        School school = schoolMapper.selectById(schoolId);
        if (school == null || !Objects.equals(school.getStatus(), 1)) {
            throw new BusinessException(40004, "学校不存在或已停用");
        }
        return school;
    }

    private Department requireDepartment(Long departmentId, Long schoolId) {
        if (departmentId == null) {
            throw new BusinessException(40004, "学院ID不能为空");
        }
        Department department = departmentMapper.selectById(departmentId);
        if (department == null || !Objects.equals(department.getSchoolId(), schoolId) || !Objects.equals(department.getStatus(), 1)) {
            throw new BusinessException(40004, "学院不存在或未隶属该学校");
        }
        return department;
    }

    private void validateStudentIdentity(String studentId, Long schoolId, Long departmentId) {
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getStudentId, studentId)
                        .eq(Student::getSchoolId, schoolId)
                        .eq(Student::getDepartmentId, departmentId)
                        .eq(Student::getStatus, 1)
        );
        if (student == null) {
            throw new BusinessException(40005, "学号验证失败：该学号不存在或学校/学院信息不匹配");
        }
    }

    private void ensureEducationNotDuplicated(Long userId, Long schoolId, String studentId) {
        Long exists = userEducationMapper.selectCount(
                new LambdaQueryWrapper<UserEducation>()
                        .eq(UserEducation::getUserId, userId)
                        .eq(UserEducation::getSchoolId, schoolId)
                        .eq(UserEducation::getStudentId, studentId)
        );
        if (exists != null && exists > 0) {
            throw new BusinessException(40006, "教育经历已存在");
        }
    }

    private void clearPrimaryEducation(Long userId) {
        UserEducation update = new UserEducation();
        update.setIsPrimary(false);
        userEducationMapper.update(update, new LambdaQueryWrapper<UserEducation>()
                .eq(UserEducation::getUserId, userId)
                .eq(UserEducation::getDeleted, 0));
    }

    private UserEducation buildEducationEntity(AddEducationDTO addEducationDTO, Long userId, String studentId, String startDate, String endDate, boolean isPrimary) {
        UserEducation entity = new UserEducation();
        entity.setUserId(userId);
        entity.setSchoolId(addEducationDTO.getSchoolId());
        entity.setDepartmentId(addEducationDTO.getDepartmentId());
        entity.setStudentId(studentId);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setIsPrimary(isPrimary);
        entity.setStatus(STATUS_VERIFIED);
        return entity;
    }

    private EducationVO buildEducationVO(UserEducation record, School school, Department department, String token) {
        EducationVO vo = new EducationVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setSchoolId(record.getSchoolId());
        vo.setSchool(school != null ? school.getName() : null);
        vo.setDepartmentId(record.getDepartmentId());
        vo.setDepartment(department != null ? department.getName() : null);
        vo.setStudentId(record.getStudentId());
        vo.setStartDate(record.getStartDate());
        vo.setEndDate(record.getEndDate());
        vo.setPrimary(Boolean.TRUE.equals(record.getIsPrimary()));
        vo.setStatus(record.getStatus());
        vo.setCreatedAt(record.getCreatedAt());
        vo.setToken(token);
        return vo;
    }

    private String refreshLoginToken(User user, Long primarySchoolId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("account", user.getAccount());
        claims.put("nickname", user.getNickname());
        claims.put("avatar", user.getAvatar());
        claims.put("schoolId", primarySchoolId);
        return JwtUtil.generateToken(
                claims,
                jwtProperties.getSecret(),
                jwtProperties.getExpiration()
        );
    }

//    private void updateUserSchoolInfo(Long userId, School school, Department department, String studentId) {
//        User updateEntity = new User();
//        updateEntity.setId(userId);
//        updateEntity.setSchoolId(school.getId());
//        updateEntity.setSchool(school.getName());
//        updateEntity.setDepartment(department != null ? department.getName() : null);
//        updateEntity.setStudentId(studentId);
//
//        int rows = userMapper.updateById(updateEntity);
//        if (rows <= 0) {
//            throw new BusinessException(50001, "更新用户学校信息失败，请稍后重试");
//        }
//    }

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

    private Long requireLogin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }
        return userId;
    }

    private void validateYearMonth(String value) {
        if (!YM_PATTERN.matcher(value).matches()) {
            throw new BusinessException(40004, "时间格式必须为YYYY-MM");
        }
    }
}

package com.unisport.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.common.BusinessException;
import com.unisport.config.JwtProperties;
import com.unisport.dto.LoginDTO;
import com.unisport.dto.RegisterDTO;
import com.unisport.entity.Student;
import com.unisport.entity.User;
import com.unisport.entity.UserEducation;
import com.unisport.mapper.StudentMapper;
import com.unisport.mapper.UserEducationMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.service.AuthService;
import com.unisport.utils.JwtUtil;
import com.unisport.vo.LoginVO;
import com.unisport.vo.RegisterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String EDUCATION_STATUS_VERIFIED = "verified";

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final UserEducationMapper userEducationMapper;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterVO register(RegisterDTO registerDTO) {
        log.info("注册账号为{}", registerDTO.getAccount());

        validateAccountUnique(registerDTO.getAccount());
        validateStudentIdUnique(registerDTO.getStudentId());
        validateStudentIdentity(registerDTO);

        User user = buildUser(registerDTO);

        int rows = userMapper.insert(user);
        if (rows <= 0) {
            log.error("注册失败，数据库插入失败，账号：{}", registerDTO.getAccount());
            throw new BusinessException(50001, "注册失败，请稍后重试");
        }

        // ?????????ID???
        log.info("用户注册成功，用户ID：{}，账号：{}", user.getId(), user.getAccount());

        createPrimaryEducationRecord(user, registerDTO);

        return buildRegisterVO(user);
    }

    private void validateAccountUnique(String account) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        Long count = userMapper.selectCount(queryWrapper);

        if (count != null && count > 0) {
            // ??????????????
            log.warn("账号已存在：{}", account);
            throw new BusinessException(40001, "账号已存在，请替换账号");
        }
    }

    private void validateStudentIdUnique(String studentId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStudentId, studentId);
        Long count = userMapper.selectCount(queryWrapper);

        if (count != null && count > 0) {
            // ????????
            log.warn("学号已被注册：{}", studentId);
            throw new BusinessException(40002, "该学号已被注册，一个学号只能注册一个账号");
        }
    }

    private void validateStudentIdentity(RegisterDTO registerDTO) {
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getStudentId, registerDTO.getStudentId())
                .eq(Student::getSchoolId, registerDTO.getSchoolId())
                .eq(Student::getDepartmentId, registerDTO.getDepartmentId())
                .eq(Student::getStatus, 1);

        Student student = studentMapper.selectOne(queryWrapper);
        if (student == null) {
            // ????????????????/??
            log.warn("学号验证失败：学号={}，学校ID={}，学院ID={}", registerDTO.getStudentId(), registerDTO.getSchoolId(), registerDTO.getDepartmentId());
            throw new BusinessException(40005, "学号验证失败：该学号不存在或学校/学院信息不匹配");
        }

        // ??????????????
        log.debug("学生身份验证通过：学号={}，姓名={}", student.getStudentId(), student.getName());
    }

    private User buildUser(RegisterDTO registerDTO) {
        User user = new User();
        user.setAccount(registerDTO.getAccount());

        String hashedPassword = BCrypt.hashpw(registerDTO.getPassword());
        user.setPassword(hashedPassword);

        user.setNickname(registerDTO.getAccount());
        user.setStudentId(registerDTO.getStudentId());
        user.setStatus(1);

        // ?????????????
        log.debug("用户实体构建完成，账号：{}", user.getAccount());
        return user;
    }

    private void createPrimaryEducationRecord(User user, RegisterDTO registerDTO) {
        UserEducation education = new UserEducation();
        education.setUserId(user.getId());
        education.setSchoolId(registerDTO.getSchoolId());
        education.setDepartmentId(registerDTO.getDepartmentId());
        education.setStudentId(registerDTO.getStudentId());
        education.setIsPrimary(true);
        education.setStatus(EDUCATION_STATUS_VERIFIED);

        int insertedRows = userEducationMapper.insert(education);
        if (insertedRows <= 0) {
            // ??????????????
            log.error("注册失败，教育经历插入失败，账号：{}", registerDTO.getAccount());
            throw new BusinessException(50001, "注册失败，请稍后重试");
        }
    }

    private RegisterVO buildRegisterVO(User user) {
        return RegisterVO.builder()
                .id(user.getId())
                .account(user.getAccount())
                .nickname(user.getNickname())
                .studentId(user.getStudentId())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // ?????????????
        log.info("开始处理用户登录，账号：{}", loginDTO.getAccount());

        try {
            User user = getUserByAccount(loginDTO.getAccount());
            if (user == null) {
                // ??????????
                log.warn("登录失败：账号不存在，账号：{}", loginDTO.getAccount());
                throw new BusinessException(40003, "账号或密码错误，请检查后重试");
            }

            validateAccountStatus(user);
            validatePassword(loginDTO.getPassword(), user.getPassword(), loginDTO.getAccount());

            Long primarySchoolId = resolvePrimarySchoolId(user);

            String token = generateToken(user, primarySchoolId);

            LoginVO loginVO = buildLoginVO(token, user);

            

            // ????????ID???
            log.info("用户登录成功，用户ID：{}，账号：{}", user.getId(), user.getAccount());
            return loginVO;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // ????????????????
            log.error("登录过程发生异常，账号：{}", loginDTO.getAccount(), e);
            throw new BusinessException(50000, "登录失败，系统异常，请稍后重试");
        }
    }

    private User getUserByAccount(String account) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        return userMapper.selectOne(queryWrapper);
    }

    private void validateAccountStatus(User user) {
        if (user.getStatus() == null || user.getStatus() != 1) {
            // ??????????????
            log.warn("登录失败：账号已被禁用，账号：{}，状态：{}", user.getAccount(), user.getStatus());
            throw new BusinessException(40004, "您的账号已被禁用，如有疑问请联系管理员");
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword, String account) {
        boolean isPasswordCorrect = BCrypt.checkpw(rawPassword, encodedPassword);
        if (!isPasswordCorrect) {
            // ????????
            log.warn("登录失败：密码错误，账号：{}", account);
            throw new BusinessException(40003, "账号或密码错误，请检查后重试");
        }
    }

    private Long resolvePrimarySchoolId(User user) {
        UserEducation primaryEducation = userEducationMapper.selectOne(
                new LambdaQueryWrapper<UserEducation>()
                        .eq(UserEducation::getUserId, user.getId())
                        .eq(UserEducation::getIsPrimary, true)
                        .eq(UserEducation::getStatus, EDUCATION_STATUS_VERIFIED)
                        .orderByDesc(UserEducation::getUpdatedAt)
                        .orderByDesc(UserEducation::getCreatedAt)
                        .last("LIMIT 1")
        );

        if (primaryEducation != null && primaryEducation.getSchoolId() != null) {
            return primaryEducation.getSchoolId();
        }

        UserEducation verifiedEducation = userEducationMapper.selectOne(
                new LambdaQueryWrapper<UserEducation>()
                        .eq(UserEducation::getUserId, user.getId())
                        .eq(UserEducation::getStatus, EDUCATION_STATUS_VERIFIED)
                        .orderByDesc(UserEducation::getUpdatedAt)
                        .orderByDesc(UserEducation::getCreatedAt)
                        .last("LIMIT 1")
        );

        return verifiedEducation != null ? verifiedEducation.getSchoolId() : null;
    }

    private String generateToken(User user, Long primarySchoolId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("account", user.getAccount());
        claims.put("nickname", user.getNickname());
        claims.put("avatar", user.getAvatar());
        claims.put("schoolId", primarySchoolId);

        String token = JwtUtil.generateToken(
                claims,
                jwtProperties.getSecret(),
                jwtProperties.getExpiration()
        );
        // Token ?????????ID
        log.debug("JWT Token生成成功，用户ID：{}", user.getId());
        return token;
    }

    private LoginVO buildLoginVO(String token, User user) {
        LoginVO.UserInfo userInfo = LoginVO.UserInfo.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();

        return LoginVO.builder()
                .token(token)
                .user(userInfo)
                .build();
    }
}

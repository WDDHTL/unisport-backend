package com.unisport.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.common.BusinessException;
import com.unisport.config.JwtProperties;
import com.unisport.dto.LoginDTO;
import com.unisport.dto.RegisterDTO;
import com.unisport.entity.Student;
import com.unisport.entity.User;
import com.unisport.mapper.StudentMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.service.AuthService;
import com.unisport.util.JwtUtil;
import com.unisport.vo.LoginVO;
import com.unisport.vo.RegisterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证授权服务实现类
 * 实现用户注册、登录等功能
 *
 * @author UniSport Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final JwtProperties jwtProperties;

    /**
     * 用户注册
     * 
     * 业务流程：
     * 1. 校验账号是否已存在
     * 2. 校验学号是否已被注册
     * 3. 验证学生身份（学号+学校+学院+状态）
     * 4. 使用BCrypt加密密码
     * 5. 创建用户记录
     * 6. 返回注册成功的用户信息
     *
     * @param registerDTO 注册请求数据
     * @return 注册成功的用户信息
     * @throws BusinessException 40001-账号已存在, 40002-学号已被注册, 40005-学号验证失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterVO register(RegisterDTO registerDTO) {
        log.info("开始处理用户注册，账号：{}", registerDTO.getAccount());

        // 1. 校验账号唯一性
        validateAccountUnique(registerDTO.getAccount());

        // 2. 校验学号唯一性（防止一个学号被多个账号注册）
        validateStudentIdUnique(registerDTO.getStudentId());

        // 3. 验证学生身份（学号+学校+学院）
        validateStudentIdentity(registerDTO);

        // 3. 创建用户实体并加密密码
        User user = buildUser(registerDTO);

        // 4. 保存用户到数据库
        int rows = userMapper.insert(user);
        if (rows <= 0) {
            log.error("用户注册失败，数据库插入失败，账号：{}", registerDTO.getAccount());
            throw new BusinessException(50001, "注册失败，请稍后重试");
        }

        log.info("用户注册成功，用户ID：{}，账号：{}", user.getId(), user.getAccount());

        // 5. 构建并返回响应对象
        return buildRegisterVO(user);
    }

    /**
     * 校验账号唯一性
     * 
     * @param account 账号
     * @throws BusinessException 40001-账号已存在
     */
    private void validateAccountUnique(String account) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        Long count = userMapper.selectCount(queryWrapper);
        
        if (count > 0) {
            log.warn("账号已存在：{}", account);
            throw new BusinessException(40001, "账号已存在，请更换账号");
        }
    }

    /**
     * 校验学号唯一性
     * 确保一个学号只能对应一个账号，防止学号被重复注册
     * 
     * @param studentId 学号
     * @throws BusinessException 40002-学号已被注册
     */
    private void validateStudentIdUnique(String studentId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStudentId, studentId);
        Long count = userMapper.selectCount(queryWrapper);
        
        if (count > 0) {
            log.warn("学号已被注册：{}", studentId);
            throw new BusinessException(40002, "该学号已被注册，一个学号只能注册一个账号");
        }
    }

    /**
     * 验证学生身份
     * 
     * 验证规则：
     * - 学号必须存在于students表
     * - 学校ID必须匹配
     * - 学院ID必须匹配
     * - 学生状态必须为在校（status=1）
     *
     * @param registerDTO 注册请求数据
     * @throws BusinessException 40005-学号验证失败
     */
    private void validateStudentIdentity(RegisterDTO registerDTO) {
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getStudentId, registerDTO.getStudentId())
                   .eq(Student::getSchoolId, registerDTO.getSchoolId())
                   .eq(Student::getDepartmentId, registerDTO.getDepartmentId())
                   .eq(Student::getStatus, 1); // 必须是在校状态

        Student student = studentMapper.selectOne(queryWrapper);
        
        if (student == null) {
            log.warn("学号验证失败：学号={}，学校ID={}，学院ID={}", 
                    registerDTO.getStudentId(), 
                    registerDTO.getSchoolId(), 
                    registerDTO.getDepartmentId());
            throw new BusinessException(40005, "学号验证失败：该学号不存在或学校/学院信息不匹配");
        }
        
        log.debug("学生身份验证通过：学号={}，姓名={}", student.getStudentId(), student.getName());
    }

    /**
     * 构建用户实体对象
     * 
     * @param registerDTO 注册请求数据
     * @return 用户实体对象
     */
    private User buildUser(RegisterDTO registerDTO) {
        User user = new User();
        user.setAccount(registerDTO.getAccount());
        
        // 使用BCrypt加密密码（自动加盐）
        String hashedPassword = BCrypt.hashpw(registerDTO.getPassword());
        user.setPassword(hashedPassword);
        
        // 设置默认昵称为账号，用户后续可以修改
        user.setNickname(registerDTO.getAccount());
        
        // 设置学校和学院信息
        user.setSchool(registerDTO.getSchool());
        user.setDepartment(registerDTO.getDepartment());
        user.setStudentId(registerDTO.getStudentId());
        
        // 设置默认状态为正常
        user.setStatus(1);
        
        log.debug("用户实体构建完成，账号：{}", user.getAccount());
        return user;
    }

    /**
     * 构建注册响应对象
     * 
     * @param user 用户实体
     * @return 注册响应VO
     */
    private RegisterVO buildRegisterVO(User user) {
        return RegisterVO.builder()
                .id(user.getId())
                .account(user.getAccount())
                .nickname(user.getNickname())
                .school(user.getSchool())
                .department(user.getDepartment())
                .studentId(user.getStudentId())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 用户登录
     * 
     * 业务流程：
     * 1. 根据账号查询用户信息
     * 2. 验证用户是否存在
     * 3. 验证账号状态是否正常（未被禁用）
     * 4. 使用BCrypt验证密码是否正确
     * 5. 生成JWT Token
     * 6. 构建并返回登录响应（Token + 用户基本信息）
     *
     * 错误码说明：
     * - 40003: 账号或密码错误（账号不存在或密码错误）
     * - 40004: 账号已被禁用
     * - 400: 参数校验失败（账号或密码为空）
     *
     * @param loginDTO 登录请求数据
     * @return 登录成功信息（包含Token和用户基本信息）
     * @throws BusinessException 登录失败时抛出，错误信息会通过GlobalExceptionHandler返回给前端
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        log.info("开始处理用户登录，账号：{}", loginDTO.getAccount());

        try {
            // 1. 根据账号查询用户信息
            User user = getUserByAccount(loginDTO.getAccount());

            // 2. 验证用户是否存在
            if (user == null) {
                log.warn("登录失败：账号不存在，账号：{}", loginDTO.getAccount());
                throw new BusinessException(40003, "账号或密码错误，请检查后重试");
            }

            // 3. 验证账号状态是否正常
            validateAccountStatus(user);

            // 4. 验证密码是否正确
            validatePassword(loginDTO.getPassword(), user.getPassword(), loginDTO.getAccount());

            // 5. 生成JWT Token
            String token = generateToken(user);

            // 6. 构建并返回登录响应
            LoginVO loginVO = buildLoginVO(token, user);

            log.info("用户登录成功，用户ID：{}，账号：{}", user.getId(), user.getAccount());
            return loginVO;
            
        } catch (BusinessException e) {
            // 业务异常直接抛出，由全局异常处理器处理
            throw e;
        } catch (Exception e) {
            // 其他未知异常
            log.error("登录过程发生异常，账号：{}", loginDTO.getAccount(), e);
            throw new BusinessException(50000, "登录失败，系统异常，请稍后重试");
        }
    }

    /**
     * 根据账号查询用户信息
     * 
     * @param account 账号
     * @return 用户实体，如果不存在则返回null
     */
    private User getUserByAccount(String account) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 验证账号状态是否正常
     * 
     * @param user 用户实体
     * @throws BusinessException 40004-账号已被禁用
     */
    private void validateAccountStatus(User user) {
        if (user.getStatus() == null || user.getStatus() != 1) {
            log.warn("登录失败：账号已被禁用，账号：{}，状态：{}", user.getAccount(), user.getStatus());
            throw new BusinessException(40004, "您的账号已被禁用，如有疑问请联系管理员");
        }
    }

    /**
     * 验证密码是否正确
     * 
     * 使用BCrypt算法验证明文密码与加密密码是否匹配
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 加密密码
     * @param account 账号（用于日志记录）
     * @throws BusinessException 40003-密码错误
     */
    private void validatePassword(String rawPassword, String encodedPassword, String account) {
        boolean isPasswordCorrect = BCrypt.checkpw(rawPassword, encodedPassword);
        if (!isPasswordCorrect) {
            log.warn("登录失败：密码错误，账号：{}", account);
            throw new BusinessException(40003, "账号或密码错误，请检查后重试");
        }
    }

    /**
     * 生成JWT Token
     * 
     * 将用户ID和账号信息编码到Token中
     *
     * @param user 用户实体
     * @return JWT Token字符串
     */
    private String generateToken(User user) {
        String token = JwtUtil.generateToken(
                user.getId(),
                user.getAccount(),
                jwtProperties.getSecret(),
                jwtProperties.getExpiration()
        );
        log.debug("JWT Token生成成功，用户ID：{}", user.getId());
        return token;
    }

    /**
     * 构建登录响应对象
     * 
     * @param token JWT Token
     * @param user 用户实体
     * @return 登录响应VO
     */
    private LoginVO buildLoginVO(String token, User user) {
        // 构建用户基本信息
        LoginVO.UserInfo userInfo = LoginVO.UserInfo.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();

        // 构建登录响应
        return LoginVO.builder()
                .token(token)
                .user(userInfo)
                .build();
    }
}

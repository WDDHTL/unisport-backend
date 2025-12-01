package com.unisport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.entity.Student;
import com.unisport.mapper.StudentMapper;
import com.unisport.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 学生服务实现类
 * 提供学生信息查询和验证服务的具体实现
 *
 * @author UniSport Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    /**
     * 验证学生身份
     * 检查学号、学校ID、学院ID是否匹配，以及学生是否在校
     *
     * @param studentId 学号
     * @param schoolId 学校ID
     * @param departmentId 学院ID
     * @return 验证是否通过
     */
    @Override
    public boolean validateStudent(String studentId, Long schoolId, Long departmentId) {
        log.debug("开始验证学生身份: studentId={}, schoolId={}, departmentId={}", 
                  studentId, schoolId, departmentId);
        
        // 查询学生信息
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentId, studentId)
               .eq(Student::getSchoolId, schoolId)
               .eq(Student::getDepartmentId, departmentId)
               .eq(Student::getStatus, 1); // 在校状态
        
        Student student = studentMapper.selectOne(wrapper);
        
        boolean valid = student != null;
        log.debug("学生身份验证结果: {}", valid ? "通过" : "不通过");
        
        return valid;
    }
}

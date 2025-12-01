package com.unisport.service;

/**
 * 学生服务接口
 * 提供学生信息查询和验证服务
 *
 * @author UniSport Team
 */
public interface StudentService {

    /**
     * 验证学生身份
     * 检查学号、学校ID、学院ID是否匹配，以及学生是否在校
     *
     * @param studentId 学号
     * @param schoolId 学校ID
     * @param departmentId 学院ID
     * @return 验证是否通过
     */
    boolean validateStudent(String studentId, Long schoolId, Long departmentId);
}

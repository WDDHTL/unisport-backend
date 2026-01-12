package com.unisport.service;

import com.unisport.vo.EducationVO;
import com.unisport.dto.AddEducationDTO;

import java.util.List;

/**
 * 教育经历领域服务接口
 */
public interface EducationService {

    /**
     * 添加教育经历
     *
     * @param addEducationDTO 新增教育经历参数
     * @return 新建的教育经历
     */
    EducationVO addEducation(AddEducationDTO addEducationDTO);

    /**
     * 按照 isPrimary 优先、创建时间倒序获取用户教育经历列表
     *
     * @param userId 目标用户ID
     * @return 教育经历列表
     */
    List<EducationVO> listUserEducations(Long userId);
}

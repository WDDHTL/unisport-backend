package com.unisport.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unisport.entity.School;
import com.unisport.mapper.SchoolMapper;
import com.unisport.service.SchoolService;
import com.unisport.vo.SchoolVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学校服务实现类
 * 实现学校信息查询功能
 *
 * @author UniSport Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolMapper schoolMapper;

    /**
     * 获取学校列表
     * 
     * 实现逻辑：
     * 1. 构建查询条件：status=1（只查启用的学校）
     * 2. 如果传入省份，添加省份筛选
     * 3. 如果传入城市，添加城市筛选
     * 4. 按 sort_order 排序
     * 5. 转换为VO对象返回
     *
     * @param province 省份筛选条件（可选）
     * @param city 城市筛选条件（可选）
     * @return 学校信息列表
     */
    @Override
    public List<SchoolVO> getSchoolList(String province, String city) {
        log.info("开始查询学校列表，省份：{}，城市：{}", province, city);

        // 构建查询条件
        LambdaQueryWrapper<School> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询启用状态的学校
        queryWrapper.eq(School::getStatus, 1);
        
        // 如果传入省份，添加省份筛选
        if (StrUtil.isNotBlank(province)) {
            queryWrapper.eq(School::getProvince, province);
            log.debug("添加省份筛选条件：{}", province);
        }
        
        // 如果传入城市，添加城市筛选
        if (StrUtil.isNotBlank(city)) {
            queryWrapper.eq(School::getCity, city);
            log.debug("添加城市筛选条件：{}", city);
        }
        
        // 按排序字段升序排列
        queryWrapper.orderByAsc(School::getSortOrder);

        // 查询数据库
        List<School> schoolList = schoolMapper.selectList(queryWrapper);
        log.info("查询到 {} 所学校", schoolList.size());

        // 转换为VO对象
        List<SchoolVO> schoolVOList = schoolList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return schoolVOList;
    }

    /**
     * 将学校实体转换为VO对象
     * 
     * @param school 学校实体
     * @return 学校VO对象
     */
    private SchoolVO convertToVO(School school) {
        return SchoolVO.builder()
                .id(school.getId())
                .name(school.getName())
                .code(school.getCode())
                .province(school.getProvince())
                .city(school.getCity())
                .build();
    }
}

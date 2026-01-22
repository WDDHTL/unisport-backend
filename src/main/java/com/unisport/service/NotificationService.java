package com.unisport.service;

import com.unisport.dto.NotificationQueryDTO;
import com.unisport.vo.NotificationListVO;

/**
 * 通知模块接口定义。
 */
public interface NotificationService {

    /**
     * 按照类型和分页条件获取当前登录用户的通知列表。
     *
     * @param queryDTO 查询条件（类型、页码、分页大小）
     * @return 通知分页数据及未读数量
     */
    NotificationListVO listNotifications(NotificationQueryDTO queryDTO);
}

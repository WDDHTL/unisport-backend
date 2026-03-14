package com.unisport.service;
import com.unisport.common.PageResult;
import com.unisport.dto.CreateInviteDTO;
import com.unisport.dto.InviteListQueryDTO;
import com.unisport.dto.InviteMineQueryDTO;
import com.unisport.vo.InviteListVO;

/**
 * 邀请相关服务。
 */
public interface InviteService {

    /**
     * 创建邀请。
     *
     * @param request 创建请求
     * @return 创建成功的邀请视图
     */
    InviteListVO createInvite(CreateInviteDTO request);

    /**
     * 获取邀请广场列表。
     *
     * @param query 参数
     * @return 分页数据
     */
    PageResult<InviteListVO> listInvites(InviteListQueryDTO query);

    PageResult<InviteListVO> listMyInvites(InviteMineQueryDTO query);
}

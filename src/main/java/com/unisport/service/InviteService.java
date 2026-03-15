package com.unisport.service;

import com.unisport.common.PageResult;
import com.unisport.dto.CreateInviteDTO;
import com.unisport.dto.InviteListQueryDTO;
import com.unisport.dto.InviteMineQueryDTO;
import com.unisport.dto.JoinInviteDTO;
import com.unisport.vo.InviteDetailVO;
import com.unisport.vo.InviteListVO;
import com.unisport.vo.InviteMemberDetailVO;

import java.util.List;

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

    /**
     * 加入邀请。
     *
     * @param inviteId 邀请ID
     * @param request  请求体
     * @return 邀请视图
     */
    InviteListVO joinInvite(Long inviteId, JoinInviteDTO request);

    /**
     * 退出邀请。
     *
     * @param inviteId 邀请ID
     * @return 邀请视图
     */
    InviteListVO leaveInvite(Long inviteId);

    /**
     * 取消邀请。
     *
     * @param inviteId 邀请ID
     * @return 邀请视图
     */
    InviteListVO cancelInvite(Long inviteId);

    /**
     * 获取邀请详情。
     *
     * @param inviteId 邀请ID
     * @return 邀请详情
     */
    InviteDetailVO getInviteDetail(Long inviteId);

    /**
     * 获取邀请成员列表。
     *
     * @param inviteId 邀请ID
     * @return 成员列表（active 状态）
     */
    PageResult<InviteMemberDetailVO> listInviteMembers(Long inviteId, Integer current, Integer size);
}

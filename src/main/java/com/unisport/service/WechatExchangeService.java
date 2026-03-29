package com.unisport.service;

import com.unisport.common.PageResult;
import com.unisport.dto.WechatExchangeAcceptDTO;
import com.unisport.dto.WechatExchangeCreateDTO;
import com.unisport.dto.WechatExchangeListQueryDTO;
import com.unisport.dto.WechatExchangeRejectDTO;
import com.unisport.vo.WechatExchangeRequestVO;
import com.unisport.vo.WechatExchangeStatusVO;

public interface WechatExchangeService {

    WechatExchangeRequestVO createRequest(WechatExchangeCreateDTO request);

    PageResult<WechatExchangeRequestVO> listRequests(WechatExchangeListQueryDTO query);

    WechatExchangeRequestVO getRequestDetail(Long id);

    WechatExchangeStatusVO accept(Long id, WechatExchangeAcceptDTO request);

    WechatExchangeStatusVO reject(Long id, WechatExchangeRejectDTO request);

    WechatExchangeStatusVO cancel(Long id);
}

package com.shanjupay.transaction.api.port;

import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;

import java.util.List;

public interface PayChannelService {
    List<PlatformChannelDTO> queryPlatformChannel() ;
    //绑定应用平台通道
    void bindPlatformChannelForApp(String appId, String platformChannelCodes);
    /**
     * 应用是否已经绑定了某个服务类型
     * @param appId
     * @param platformChannel
     * @return 已绑定返回1，否则 返回0
     */
    int queryAppBindPlatformChannel(String appId,String platformChannel);


    /**
     * 根据平台服务类型获取支付渠道列表
* @param platformChannelCode
* @return
        */
    List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode);


    /**
     * 保存支付渠道参数
     * @param payChannelParam 商户原始支付渠道参数
     */
    void savePayChannelParam(PayChannelParamDTO payChannelParam) ;

}

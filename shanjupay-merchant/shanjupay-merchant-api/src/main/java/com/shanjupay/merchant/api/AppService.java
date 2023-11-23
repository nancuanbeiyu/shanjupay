package com.shanjupay.merchant.api;

import com.shanjupay.merchant.api.dto.app.AppDTO;

import java.util.List;

public interface AppService {
//商户下创建应用
    AppDTO createApp(Long merchantId, AppDTO app) ;
    AppDTO getAppById(String id)  ;
    List<AppDTO> queryAppByMerchant(Long merchantId)  ;


}

package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.abnormal;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.app.AppDTO;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.mapper.AppMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppServiceImpl implements AppService {
    @Autowired
    private AppMapper appMapper;
    @Override
    public AppDTO createApp(Long merchantId, AppDTO app) {
        App appvo=new App();
        QueryWrapper<App> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("MERCHANT_ID",merchantId);
        App app1 = appMapper.selectOne(queryWrapper);
        if(app1!=null){

            throw new abnormal( CommonErrorCode.E_200240);
        }
        //appvo.setAppId(app.getAppId());
        appvo.setAppName(app.getAppName());
        appvo.setNotifyUrl(app.getNotifyUrl());
        appvo.setMerchantId(app.getMerchantId());
        appvo.setPublicKey(app.getPublicKey());
        appMapper.insert(appvo);
        return null;
    }
}

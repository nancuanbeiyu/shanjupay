package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.abnormal;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.app.AppDTO;
import com.shanjupay.merchant.cover.AppCovert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@org.apache.dubbo.config.annotation.Service
public class AppServiceImpl implements AppService {
    @Autowired
    private AppMapper appMapper;
    @Autowired
    private MerchantMapper merchantMapper;


    @Override
    public AppDTO createApp(Long merchantId, AppDTO app) {
        App appvo=new App();
        QueryWrapper<App> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("MERCHANT_ID",merchantId);
        App app1 = appMapper.selectOne(queryWrapper);
        if(app1!=null){

            throw new abnormal( CommonErrorCode.E_200240);
        }
        //查看是否通过审核

        Merchant merchant = merchantMapper.selectById(merchantId);
        if(merchant==null){
            throw new abnormal( CommonErrorCode.E_200241);
        }
        if(!merchant.getAuditStatus().equals("2") ){
            throw new abnormal( CommonErrorCode.E_200236);
        }
        //查询用户名称唯一
        QueryWrapper<App>appQueryWrapper=new QueryWrapper<>();
        appQueryWrapper.eq("APP_NAME",app.getAppName());
        App selectOne = appMapper.selectOne(appQueryWrapper);
        if(selectOne!=null){
            throw new abnormal( CommonErrorCode.E_200242);

        }
        appvo.setAppId(UUID.randomUUID()+"");
        appvo.setAppName(app.getAppName());
        appvo.setNotifyUrl(app.getNotifyUrl());
        appvo.setMerchantId(app.getMerchantId());
        appvo.setPublicKey(app.getPublicKey());
        appMapper.insert(appvo);
        return app;
    }

    @Override
    public AppDTO getAppById(String id) {
        AppDTO appDTO=new AppDTO();
        QueryWrapper<App>appQueryWrapper=new QueryWrapper<>();
        appQueryWrapper.eq("APP_ID",id);
        App selectOne = appMapper.selectOne(appQueryWrapper);
        appDTO.setAppName(selectOne.getAppName());
        appDTO.setNotifyUrl(selectOne.getNotifyUrl());
        appDTO.setPublicKey(selectOne.getPublicKey());
        appDTO.setMerchantId(selectOne.getMerchantId());

        return appDTO;
    }

    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) {
        List<App> apps = appMapper.selectList(new QueryWrapper<App>
                ().lambda().eq(App::getMerchantId,merchantId));
        List<AppDTO> appDTOS = AppCovert.INSTANCE.listentity2dto(apps);
        return appDTOS;
    }
}

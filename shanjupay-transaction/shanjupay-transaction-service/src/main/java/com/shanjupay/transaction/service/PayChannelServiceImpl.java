package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.abnormal;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.api.port.PayChannelService;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

// 支付渠道服务
@org.apache.dubbo.config.annotation.Service
public class PayChannelServiceImpl implements PayChannelService {
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private PlatformChannelMapper platformChannelMapper;

    @Autowired
    private AppPlatformChannelMapper appPlatformChannelMapper;

    @Autowired
    private PayChannelParamMapper payChannelParamMapper;

    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() {
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        List<PlatformChannelDTO> platformChannelDTOS =
                PlatformChannelConvert.INSTANCE.listentity2listdto(platformChannels);
        return platformChannelDTOS;
    }

    @Override
    @Transactional
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) {
//根据appId和平台服务类型code查询app_platform_channel

        QueryWrapper<AppPlatformChannel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("APP_ID", appId);
        queryWrapper.eq("PLATFORM_CHANNEL", platformChannelCodes);

        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(queryWrapper);
//如果没有绑定则绑定
        if (appPlatformChannel == null) {
            appPlatformChannel = new AppPlatformChannel();
            appPlatformChannel.setAppId(appId);
            appPlatformChannel.setPlatformChannel(platformChannelCodes);
            appPlatformChannelMapper.insert(appPlatformChannel);
        }
    }

    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannel) {
        QueryWrapper<AppPlatformChannel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("APP_ID", appId);
        queryWrapper.eq("PLATFORM_CHANNEL", platformChannel);
        Integer integer = appPlatformChannelMapper.selectCount(queryWrapper);
        if (integer > 0) {
            return 1;
        } else {
            return 0;
        }

    }

    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) {
        return platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
    }

    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) {
        //形参为空判断
//        if (payChannelParamDTO == null || StringUtils.isBlank(payChannelParamDTO.getAppId()) ||
//                StringUtils.isBlank(payChannelParamDTO.getPlatformChannelCode())
//                ||
//                StringUtils.isBlank(payChannelParamDTO.getPayChannel())) {
//            //throw new abnormal( CommonErrorCode.E_300009);
//        }
//根据appid和服务类型查询应用与服务类型绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParamDTO.getAppId(),
                payChannelParamDTO.getPlatformChannelCode());
        if (appPlatformChannelId == null) {
//应用未绑定该服务类型不可进行支付渠道参数配置
            throw new abnormal(CommonErrorCode.E_200243);
        }
//根据应用与服务类型绑定id和支付渠道查询参数信息
        QueryWrapper<PayChannelParam> payChannelParamQueryWrapper = new QueryWrapper<>();
        payChannelParamQueryWrapper.eq("APP_PLATFORM_CHANNEL_ID",appPlatformChannelId);
        payChannelParamQueryWrapper.eq("PAY_CHANNEL",payChannelParamDTO.getPayChannel());
        PayChannelParam payChannelParam = payChannelParamMapper.selectOne(payChannelParamQueryWrapper);

//更新已有配置
        if (payChannelParam != null) {
            payChannelParam.setChannelName(payChannelParamDTO.getChannelName());
            payChannelParam.setParam(payChannelParamDTO.getParam());
            payChannelParamMapper.updateById(payChannelParam);
        } else {
//不存在,添加新配置
            PayChannelParam entity = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParamDTO);
            entity.setId(null);
//应用与服务类型绑定id
            entity.setAppPlatformChannelId(appPlatformChannelId);
            payChannelParamMapper.insert(entity);
        }
    }



//更新缓存
    private void updateCache(String appId, String platformChannel) {
        String redisKey="SJ_PAY_PARAM:"+appId+":"+platformChannel;

//1.key的构建 如：SJ_PAY_PARAM:b910da455bc84514b324656e1088320b:shanju_c2b
        //String redisKey = redis.opsForValue().setIfPresent().keyBuilder(appId, platformChannel);
//2.查询redis,检查key是否存在
//        Boolean exists = Boolean.valueOf(redis.opsForValue().get(""));
        Boolean exists = Boolean.valueOf( redis.opsForValue().get(redisKey));
        if (exists) {
//删除原有缓存
            redis.delete(redisKey);
        }
//3.从数据库查询应用的服务类型对应的实际支付参数，并重新存入缓存
        List<PayChannelParamDTO> paramDTOS =
                queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        if (paramDTOS != null) {
//存入缓存
            redis.opsForValue().set(redisKey, JSON.toJSON(paramDTOS).toString());
        }
    }
    //查询缓存
//    public List<PayChannelParamDTO> selectCache(String appId, String platformChannel){
//        String redisKey="SJ_PAY_PARAM:"+appId+":"+platformChannel;
//        String s = redis.opsForValue().get(redisKey);
//        List<String> list =  Arrays.asList(s.split(","));
//        String s1 = JSON.toJSONString(list);
//
//        return null;
//    }

    /**
     *
     *
     * @param appId           应用id
     * @param platformChannel 服务类型代码
     * @return
     */
    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) {
//查出应用id和服务类型代码在app_platform_channel主键
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
//根据appPlatformChannelId从pay_channel_param查询所有支付参数
        QueryWrapper<PayChannelParam> ParamQueryWrapper = new QueryWrapper<>();
        ParamQueryWrapper.eq("APP_PLATFORM_CHANNEL_ID", appPlatformChannelId);
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(ParamQueryWrapper);
        return PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
    }

    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) {
        List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        for (PayChannelParamDTO payChannelParam : payChannelParamDTOS) {
            if (payChannelParam.getPayChannel().equals(payChannel)) {
                return payChannelParam;
            }

        }
        return null;
    }


    /**
     * 根据appid和服务类型查询应用与服务类型绑定id
     *
     *
     *
     * @param appId
     * @param platformChannelCode
     * @return
     */
    private Long selectIdByAppPlatformChannel(String appId, String platformChannelCode) {
        //根据appid和服务类型查询应用与服务类型绑定id
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new
                LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        if (appPlatformChannel != null) {
            return appPlatformChannel.getId();
        }
        return null;
    }

}

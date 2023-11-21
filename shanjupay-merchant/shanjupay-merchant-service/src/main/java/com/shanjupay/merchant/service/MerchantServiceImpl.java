package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.abnormal;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.MerchantRegisterVo;
 import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.MerchantMapper;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shanjupay.common.domain.CommonErrorCode.*;

/**
 * Created by Administrator.
 */
@org.apache.dubbo.config.annotation.Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private StringRedisTemplate redis;
    @Value("${ApplyFor.key}")
    private String key;
    @Value("${ApplyFor.LoseKey}")
    private String LoseKey;
    @Override
    public MerchantDTO queryMerchantById(int id) {
        Merchant merchant = merchantMapper.selectById(id);
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setId(merchant.getId());
        merchantDTO.setMerchantName(merchant.getMerchantName());
        //....
        return merchantDTO;
    }


    @Override
    public com.shanjupay.merchant.api.dto.Merchant loginMerchantDTO(MerchantRegisterVo dto) throws abnormal {

        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", dto.getMobile());
        if (merchantMapper.selectOne(queryWrapper) != null) {

            throw new abnormal(CommonErrorCode.E_200203);
        }
        //手机号格式校验
        String mobileRegEx = "^1[3,4,5,6,7,8,9][0-9]{9}$";//正则表达式

        Pattern pattern = Pattern.compile(mobileRegEx);//函数语法 匹配的正则表达式
        Matcher matcher = pattern.matcher(dto.getMobile());//进行匹配

        if (!matcher.matches()) {//校验手机号格式是否正确，若是匹配成功则返回true

            throw new abnormal(CommonErrorCode.E_200224);
        }



        //验证码验证
        verify(dto.getMobile(), dto.getVerifiyCode());

        Merchant merchant = new Merchant();
        merchant.setMobile(dto.getMobile());
        merchant.setAuditStatus("0");
        merchantMapper.insert(merchant);
        com.shanjupay.merchant.api.dto.Merchant merchantMax = new com.shanjupay.merchant.api.dto.Merchant();
        merchantMax.setMobile(dto.getMobile());
        merchantMax.setAuditStatus("0");
        return merchantMax;
    }


    //生成限时验证码
    @Override
    public String ApplyFor(String telephone) throws Exception {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 创建Redisson客户端
        RedissonClient redisson = Redisson.create(config);
        String s = redis.opsForValue().get(key + telephone);
        if (s == null) {
            Random random = new Random();
            String randomNum = Integer.toString(random.nextInt(900000) + 100000);
            redis.opsForValue().set(key + telephone, randomNum, 600, TimeUnit.SECONDS);
            return randomNum;
        }
        long ttl = redisson.getBucket(key + telephone).remainTimeToLive() / 1000;
        throw new Exception("再次可发送验证码剩余时间:" + ttl);

    }

    //在验证码生效的基础上查询失败次数
    @Override
    public Long Countdown(String userId, boolean b) {

        // 创建Redisson配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 创建Redisson客户端
        RedissonClient redisson = Redisson.create(config);

        //查询redis失败次数
        Integer s = Integer.parseInt(redis.opsForValue().get(LoseKey + userId) == null ? "0" : redis.opsForValue().get(LoseKey + userId).toString());

        if (s < 3) {
            //每次登录时查询失败次数
            if (b) {
                return null;
            }
            redis.opsForValue().set(LoseKey + userId, String.valueOf(s + 1), 3600, TimeUnit.SECONDS);
            return 0L;
        }
        //查询剩余时间/按秒
        long ttl = redisson.getBucket(LoseKey + userId).remainTimeToLive() / 1000;
        if (ttl > 60) {
            redis.expire(LoseKey + userId, 60, TimeUnit.SECONDS);
            return 60L;
        }
        // 获取键的过期剩余时间
        ttl = redisson.getBucket(LoseKey + userId).remainTimeToLive() / 1000;

        // 关闭Redisson客户端
        redisson.shutdown();

        return ttl;
    }



    //校验验证码
    @Override
    public boolean verify(String phone, String code) throws abnormal {
        String s = redis.opsForValue().get(key + phone);
        if (s == null) {

            throw new abnormal(CommonErrorCode.E_200238);
        }
        if (!code.toString().equals(s.toString())) {

            throw new abnormal( CommonErrorCode.E_200237);
        }
        return true;
    }





}

package com.shanjupay.merchant.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.abnormal;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.MerchantRegisterVo;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.cover.MerchantCovert;
import com.shanjupay.merchant.cover.MerchantCovertImpl;
import com.shanjupay.merchant.cover.StaffConvert;
import com.shanjupay.merchant.cover.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;

import com.shanjupay.common.vo.MerchantDetailVO;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
 import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private StaffMapper staffMapper;
    @Autowired
    private StoreStaffMapper storeStaffMapper;
    @org.apache.dubbo.config.annotation.Reference
     private  TenantService tenantService;
//    @org.apache.dubbo.config.annotation.Reference
//    private MerchantCovertImpl merchantCovertImpl;

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
        MerchantDTO merchantDTO = MerchantCovert.INSTANCE.entity2dto(merchant);
        merchantDTO.setPassword("12121");
         createMerchant(merchantDTO);

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

            throw new abnormal(CommonErrorCode.E_200237);
        }
        return true;
    }

    @Override
    public void application(long id, MerchantDetailVO merchantDTO) {
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", id);
        Merchant merchant = merchantMapper.selectOne(queryWrapper);
        if (merchant == null) {
            throw new abnormal(CommonErrorCode.E_200227);
        }
        merchant.setMerchantName(merchantDTO.getMerchantName());
        merchant.setMerchantNo(merchantDTO.getMerchantNo());
        merchant.setMerchantAddress(merchantDTO.getMerchantAddress());
        merchant.setMerchantType(merchantDTO.getMerchantType());
        merchant.setBusinessLicensesImg(merchantDTO.getBusinessLicensesImg());
        merchant.setIdCardFrontImg(merchantDTO.getIdCardFrontImg());
        merchant.setIdCardAfterImg(merchantDTO.getIdCardAfterImg());
        merchant.setUsername(merchantDTO.getUsername());
        merchant.setContactsAddress(merchantDTO.getContactsAddress());
        //merchant.setTenantId(merchantDTO.getTenantId());
        merchant.setAuditStatus("1");
//        MerchantDTO merchantdto = new MerchantCovertImpl().entity2dto(merchant);
//        createMerchant(merchantdto);
        merchantMapper.updateById(merchant);
    }

    @Override
    public StoreDTO createStore(StoreDTO storeDTO) {
        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        System.err.println("商户下新增门店" + JSON.toJSONString(store));
        QueryWrapper<Store> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store_name", storeDTO.getStoreName());
        Store storesSelect = storeMapper.selectOne(queryWrapper);
        if (storesSelect != null) {
            //门店名称重复
            throw new abnormal(CommonErrorCode.E_200244);
        }
        storeMapper.insert(store);
        return StoreConvert.INSTANCE.entity2dto(store);
    }

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) {
//1.校验手机号格式及是否存在
        String mobile = staffDTO.getMobile();

//根据商户id和手机号校验唯一性
        if (isExistStaffByMobile(mobile, staffDTO.getMerchantId())) {
            throw new abnormal(CommonErrorCode.E_200203);
        }
        //2.校验用户名是否为空
        String username = staffDTO.getUsername();
//根据商户id和账号校验唯一性
        if (isExistStaffByUserName(username, staffDTO.getMerchantId())) {
            throw new abnormal(CommonErrorCode.E_200245 );
        }
        Staff entity = StaffConvert.INSTANCE.dto2entity(staffDTO);
        System.err.println("商户下新增员工");
        staffMapper.insert(entity);
        return StaffConvert.INSTANCE.entity2dto(entity);
    }

    /**
     * 根据手机号判断员工是否已在指定商户存在
     * @param mobile 手机号
     * @return
     */
    private boolean isExistStaffByMobile(String mobile, Long merchantId) {
        LambdaQueryWrapper<Staff> lambdaQueryWrapper = new LambdaQueryWrapper<Staff>();
        lambdaQueryWrapper.eq(Staff::getMobile, mobile).eq(Staff::getMerchantId, merchantId);
        int i = staffMapper.selectCount(lambdaQueryWrapper);
        return i > 0;
    }
    /**
     * 根据账号判断员工是否已在指定商户存在
     * @param userName
     * @param merchantId
     * @return
     */
    private boolean isExistStaffByUserName(String userName, Long merchantId) {
        LambdaQueryWrapper<Staff> lambdaQueryWrapper = new LambdaQueryWrapper<Staff>();
        lambdaQueryWrapper.eq(Staff::getUsername, userName).eq(Staff::getMerchantId,
                merchantId);
        int i = staffMapper.selectCount(lambdaQueryWrapper);
        return i > 0;
    }

    @Override
    public void bindStaffToStore(Long storeId, Long staffId) {
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStoreId(storeId);
        storeStaff.setStaffId(staffId);
        storeStaffMapper.insert(storeStaff);
    }

    @Transactional
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) {
// 1.校验
        if (merchantDTO == null) {
            throw new abnormal(CommonErrorCode.E_200202 );
        }
//手机号非空校验
//        if (StringUtils.isBlank(merchantDTO.getMobile())) {
//          //  throw new BusinessException(CommonErrorCode.E_100112);
//        }
//校验手机号的合法性
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())) {
            throw new abnormal(CommonErrorCode.E_200246 );
        }
//联系人非空校验
//        if (StringUtils.isBlank(merchantDTO.getUsername())) {
//           // throw new BusinessException(CommonErrorCode.E_100110);
//        }
//密码非空校验
//        if (StringUtils.isBlank(merchantDTO.getPassword())) {
//           // throw new BusinessException(CommonErrorCode.E_100111);
//        }
//校验商户手机号的唯一性,根据商户的手机号查询商户表，如果存在记录则说明已有相同的手机号重复
//        LambdaQueryWrapper<Merchant> lambdaQryWrapper = new LambdaQueryWrapper<Merchant>()
//                .eq(Merchant::getMobile,merchantDTO.getMobile());
        QueryWrapper<Merchant> merchantQueryWrapper=new QueryWrapper<>();
        merchantQueryWrapper.eq("MOBILE",merchantDTO.getMobile());
        Integer count = merchantMapper.selectCount(merchantQueryWrapper);
//        if(count>0){
//            throw new abnormal(CommonErrorCode.E_200203 );
//        }
//2.添加租户 和账号 并绑定关系
        CreateTenantRequestDTO createTenantRequest = new CreateTenantRequestDTO();
        createTenantRequest.setMobile(merchantDTO.getMobile());
//表示该租户类型是商户
        createTenantRequest.setTenantTypeCode("shanju‐merchant");
//设置租户套餐为初始化套餐餐
        createTenantRequest.setBundleCode("shanju‐merchant");
//租户的账号信息
        createTenantRequest.setUsername(merchantDTO.getUsername());
        createTenantRequest.setPassword(merchantDTO.getPassword());
//新增租户并设置为管理员
        createTenantRequest.setName(merchantDTO.getUsername());
       // log.info("商户中心调用统一账号服务，新增租户和账号");
        TenantDTO tenantDTO = tenantService.createTenantAndAccount(createTenantRequest);
        if (tenantDTO == null || tenantDTO.getId() == null) {
            throw new abnormal(CommonErrorCode.E_200247 );
        }
//判断租户下是否已经注册过商户
        //QueryWrapper queryWrapper=new QueryWrapper();
        Merchant merchant = merchantMapper.selectOne(new QueryWrapper<Merchant>().lambda().eq(Merchant::getTenantId, tenantDTO.getId()));
        if (merchant != null && merchant.getId() != null) {
            throw new abnormal(CommonErrorCode.E_200248 );
        }
//3. 设置商户所属租户
        merchantDTO.setTenantId(tenantDTO.getId());
//设置审核状态，注册时默认为"0"
        merchantDTO.setAuditStatus("0");//审核状态 0‐未申请,1‐已申请待审核,2‐审核通过,3‐审核拒绝
        Merchant entity = MerchantCovert.INSTANCE.dto2entity(merchantDTO);
//保存商户信息
        //merchantMapper.insert(entity);
//4.新增门店，创建根门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(entity.getId());
        storeDTO.setStoreName("根门店");
        storeDTO = createStore(storeDTO);
        System.err.println("门店信息：{}" + JSON.toJSONString(storeDTO));
//5.新增员工，并设置归属门店
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMerchantId(entity.getId());
        staffDTO.setMobile(merchantDTO.getMobile());
        staffDTO.setUsername(merchantDTO.getUsername());
//为员工选择归属门店,此处为根门店
        staffDTO.setStoreId(storeDTO.getId());
        staffDTO = createStaff(staffDTO);
//6.为门店设置管理员
        bindStaffToStore(storeDTO.getId(), staffDTO.getId());
//返回商户注册消息
        return MerchantCovert.INSTANCE.entity2dto(entity);
    }

}
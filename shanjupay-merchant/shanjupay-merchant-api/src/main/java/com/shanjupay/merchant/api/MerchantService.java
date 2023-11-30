package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.abnormal;
import com.shanjupay.common.vo.MerchantDetailVO;
import com.shanjupay.merchant.api.dto.*;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;


/**
 * Created by Administrator.
 */
public interface MerchantService {

    //根据 id查询商户
    public MerchantDTO queryMerchantById(int id);
    //注册商户
      Merchant loginMerchantDTO(MerchantRegisterVo dto) throws abnormal;
      String ApplyFor(String telephone) throws Exception;
      Long Countdown(String userId,boolean b);
      boolean verify(String phone,String code) throws abnormal;
      // 资质申请
    void application(long id, MerchantDetailVO merchantDTO);
    /**
     * 商户下新增门店
     * @param storeDTO
     */
    StoreDTO createStore(StoreDTO storeDTO) ;
    /**
     * 商户新增员工
     * @param staffDTO
     */
    StaffDTO createStaff(StaffDTO staffDTO) ;
    /**
     * 为门店设置管理员
     * @param storeId
     * @param staffId
     * @throws
     */
    void bindStaffToStore(Long storeId, Long staffId) ;


    /**
     * 创建租户如果已存在租户则返回租户信息，否则新增租户、新增租户管理员，同时初始化权限
     * 1.若管理员用户名已存在，禁止创建
     * 2.手机号已存在，禁止创建
     * 3.创建根租户对应账号时，需要手机号，账号的用户名密码
     * @return
     */
   // TenantDTO createTenantAndAccount(CreateTenantRequestDTO createTenantRequest);
      MerchantDTO createMerchant(MerchantDTO merchantDTO);

    }

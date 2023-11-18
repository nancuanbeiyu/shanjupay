package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.abnormal;
import com.shanjupay.merchant.api.dto.Merchant;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.MerchantRegisterVo;

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
}

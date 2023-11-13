package com.shanjupay.merchant.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MerchantRegisterVo implements Serializable {
    private  String mobile;
    private String username;
    private String password;
    private  String verifiyKey;
    private   String verifiyCode;
}

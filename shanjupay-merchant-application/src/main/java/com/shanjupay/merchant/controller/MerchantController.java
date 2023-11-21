package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.abnormal;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.Merchant;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.MerchantRegisterVo;
import com.shanjupay.merchant.config.fileQiNiuYunlmpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Administrator
 * @version 1.0
 **/
@RestController
@Api(value="商户平台应用接口",tags = "商户平台应用接口",description = "商户平台应用接口")
public class MerchantController {

    @org.apache.dubbo.config.annotation.Reference
   private MerchantService merchantService;
    @Autowired
    private fileQiNiuYunlmpl fileQiNiuYun;

    @ApiOperation(value="根据id查询商户信息")
    @GetMapping("/merchants")
    public MerchantDTO queryMerchantById(  int id){

        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

    @ApiOperation("登录失败冻结")
    @GetMapping(path = "/hello")
    public String hello(){
        long countdown = merchantService.Countdown("177200352020",true);

        return countdown+"";
    }

    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name = "photo", value = "手机号", required = true, dataType = "string",paramType = "query")
    @GetMapping(value = "/sms")
    public String ApplyFor(String photo) throws Exception {
        String code = merchantService.ApplyFor(photo);
        return code;
    }

    //注册
    @ApiOperation("注册")
    @PostMapping(value = "/merchants/register")
    public com.shanjupay.merchant.api.dto.Merchant ApplyFor(@RequestBody MerchantRegisterVo Merchant) throws abnormal  {
        com.shanjupay.merchant.api.dto.Merchant merchant = merchantService.loginMerchantDTO(Merchant);
        return merchant;
    }


    @ApiOperation("测试，后期加入登录验证验证码")
    //@ApiImplicitParam(name = "name", value = "姓名", required = true, dataType = "string")
    @PostMapping(value = "/verify")
    public boolean verify(String photo,String code) throws abnormal {
        boolean b= merchantService.verify(photo,code);
        return b;
    }

    @ApiOperation("文件上传")
    //@ApiImplicitParam(name = "name", value = "姓名", required = true, dataType = "string")
    @PostMapping(value = "/upload")
    public String  upload(@ApiParam(required = true) @RequestBody MultipartFile filer) throws abnormal, IOException {
        //获取文件拓展名
        int i = filer.getOriginalFilename().lastIndexOf(".");
        String substring = filer.getOriginalFilename().substring(i);
        String FileName= UUID.randomUUID()+substring;
        String s = fileQiNiuYun.FileUpload(filer.getBytes(), FileName);
        return s;
    }


}

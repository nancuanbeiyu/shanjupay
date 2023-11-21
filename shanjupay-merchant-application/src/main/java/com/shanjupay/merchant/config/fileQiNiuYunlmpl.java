package com.shanjupay.merchant.config;

import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.abnormal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class fileQiNiuYunlmpl {
    @Value("${ApplyFor.qiniuyun.accessKey}")
    private   String accessKey ;
    @Value("${ApplyFor.qiniuyun.secretKey}")
    private   String secretKey ;
    @Value("${ApplyFor.qiniuyun.bucket}")
    private   String bucket ;
    @Value("${ApplyFor.qiniuyun.url}")
    private   String url ;


    //文件上传

    public String FileUpload(byte[] file, String FileName) {

        try {
            QiniuUtils.testUpload (accessKey,secretKey,bucket,file,FileName);

        }catch (Exception e){
            throw new abnormal(CommonErrorCode.E_200239);

        }

        return url+FileName;
    }
}

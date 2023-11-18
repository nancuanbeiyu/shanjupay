package com.shanjupay.merchant.config;

import com.alibaba.cloud.dubbo.http.HttpServerRequest;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.abnormal;
import com.shanjupay.common.util.RestErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.shanjupay.common.domain.CommonErrorCode.UNKOWN;

//全局异常
@ControllerAdvice

public class GlobalException {


    //捕获异常
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse RestErrorResponse(HttpServletRequest request, HttpServletResponse
            response, Exception e){
        RestErrorResponse restErrorResponse=null;
        if(e instanceof abnormal){
           abnormal abnormal= (abnormal)e;
            CommonErrorCode errorCode = abnormal.getErrorCode();
            restErrorResponse=new RestErrorResponse(errorCode.getCode()+"",errorCode.getDesc());
            System.err.println(restErrorResponse);
            return restErrorResponse;
        }

        restErrorResponse=new RestErrorResponse(UNKOWN.getCode()+"",UNKOWN.getDesc());
        System.err.println(restErrorResponse);
        return restErrorResponse;
    }
}

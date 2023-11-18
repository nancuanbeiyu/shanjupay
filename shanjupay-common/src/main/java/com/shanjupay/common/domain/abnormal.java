package com.shanjupay.common.domain;

import lombok.Data;

@Data
public class abnormal  extends RuntimeException{
    private  CommonErrorCode errorCode;

    public abnormal(CommonErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public abnormal( ) {
        super();
     }
}

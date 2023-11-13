package com.shanjupay.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author Administrator
 * @version 1.0
 **/

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)

public class MerchantBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(MerchantBootstrap.class,args);
    }
}

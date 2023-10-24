package com.da.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.da.usercenter.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
public class PartnerMatchingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerMatchingApplication.class, args);
    }

}

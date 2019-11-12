package com.atguigu.gmall.ums;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall.ums.dao")
@EnableSwagger2
@EnableDiscoveryClient
public class GmallUmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallUmsApplication.class, args);
    }

}

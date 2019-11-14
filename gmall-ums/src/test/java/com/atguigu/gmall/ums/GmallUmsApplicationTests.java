package com.atguigu.gmall.ums;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallUmsApplicationTests {
  /*  sms:
    host: http://dingxin.market.alicloudapi.com
    path: /dx/sendSms
    method: POST
    appcode: feadbf248bb443338d3d6db6aa300845*/
    @Value("${sms.host}")
    private String host;
    @Test
    void contextLoads() {
        System.out.println(host);
    }

}

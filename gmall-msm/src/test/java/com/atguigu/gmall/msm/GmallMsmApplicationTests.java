package com.atguigu.gmall.msm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallMsmApplicationTests {
//    @Autowired
//    private SmsTemplate smsTemplate;
    @Value("${sms.host}")
    private String host;
    @Test
    void contextLoads() {
//        Map<String, String> querys = new HashMap<String, String>();
//        querys.put("mobile", "18788831697");
//        querys.put("param", "code:" + "123455");
//        querys.put("tpl_id", "TP1711063");//短信模板
//        smsTemplate.sendCode(querys);
        System.out.println(host);
    }

}

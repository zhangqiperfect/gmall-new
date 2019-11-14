package com.atguigu.gmall.msm.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.msm.util.SmsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequestMapping("/user")
@RestController
@Slf4j
public class UserLoginController {

    @Autowired
    SmsTemplate smsTemplate;

    @Autowired
    StringRedisTemplate redisTemplate;

    @PostMapping("/sendCode")
    public Resp<Object> sendCode(String phoneNo) {
//1、生成验证码保存到服务器，准备用户提交上来进行对比
        String code = UUID.randomUUID().toString().substring(0, 4);
//2、保存验证码和手机号的对应关系,设置验证码过期时间
        redisTemplate.opsForValue().set(phoneNo, code, 30l, TimeUnit.MINUTES);
//2、短信发送构造参数
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phoneNo);
        querys.put("param", "code:" + code);
        querys.put("tpl_id", "TP1711063");//短信模板
//3、发送短信
        String sendCode = smsTemplate.sendCode(querys);
        if (sendCode.equals("") || sendCode.equals("fail")) {
//短信失败
            return Resp.fail("短信发送失败");
        }
        return Resp.ok(sendCode);
    }
}

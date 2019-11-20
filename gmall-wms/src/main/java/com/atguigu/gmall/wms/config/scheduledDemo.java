package com.atguigu.gmall.wms.config;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author ZQ
 * @create 2019-11-18 23:17
 */
@Component
public class scheduledDemo {
    //定时任务
   // @Scheduled(fixedDelay = 10000)
    public void test(){
        System.out.println("======="+ LocalDateTime.now());
    }
}

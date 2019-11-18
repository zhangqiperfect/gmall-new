package com.atguigu.gmall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ZQ
 * @create 2019-11-11 19:32
 */
@Configuration
public class ThreadPoolConfig {
  /*  @Value("${threadPoll.corePoolSize}")
    private int corePoolSize;
    @Value("${threadPoll.maximumPoolSize}")
    private int maximumPoolSize;
    @Value("${threadPoll.keepAliveTime}")
    private long keepAliveTime;
    @Value("${threadPoll.capacity}")
    private int capacity;*/

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(500, 1000, 2, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
    }
}

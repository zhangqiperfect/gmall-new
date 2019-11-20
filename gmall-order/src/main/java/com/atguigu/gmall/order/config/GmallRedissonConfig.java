package com.atguigu.gmall.order.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ZQ
 * @create 2019-11-08 23:34
 */
@Configuration
public class GmallRedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        // 可以用"rediss://"来启用SSL连接
        config.useSingleServer().setAddress("redis://192.168.80.168:6379");
        return Redisson.create(config);
    }

}

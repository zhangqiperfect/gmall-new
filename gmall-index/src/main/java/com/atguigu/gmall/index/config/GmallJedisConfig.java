package com.atguigu.gmall.index.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

/**
 * @author ZQ
 * @create 2019-11-08 23:03
 */
@Configuration
public class GmallJedisConfig {
    @Bean
    public JedisPool jedisPool(){
        return new JedisPool("192.168.80.168",6379);
    }
}

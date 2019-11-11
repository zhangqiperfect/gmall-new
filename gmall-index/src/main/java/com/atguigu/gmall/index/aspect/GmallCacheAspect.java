package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotationl.GmallCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author ZQ
 * @create 2019-11-09 16:53
 */
@Component
@Aspect
public class GmallCacheAspect {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 1、返回值object
     * 2、方法的参数ProceedingJoinPoint
     * 3、方法必须抛出throwable异常
     * 4、通过pjp.proceed(args)来执行目标方法
     */
    @Around("@annotation(com.atguigu.gmall.index.annotationl.GmallCache)")
    public Object cacheArroundAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//        获取注解参数
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        GmallCache annotation = methodSignature.getMethod().getAnnotation(GmallCache.class);
        Class returnType = methodSignature.getReturnType();
        String prefix = annotation.prefix();
        long timeout = annotation.timeout();
        long random = annotation.random();
        timeout = timeout + (long) Math.random() * random;
        String args = Arrays.asList(proceedingJoinPoint.getArgs()).toString();
        String key = prefix + ":" + args;

        //查询缓存
        Object result = cacheHit(key, returnType);
        if (result != null) {
            return result;
        }
//        加分布式锁
        RLock lock = this.redissonClient.getLock("lock" + args);
        lock.lock();
//        查询缓存
//        如果缓存中有直接返回，并且释放分布式锁
        result = cacheHit(key, returnType);
        if (result != null) {
            lock.unlock();
            return result;
        }
        result = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
//         放入缓存，释放分布式锁
        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result), timeout, TimeUnit.SECONDS);
        lock.unlock();
        return result;
    }

    public Object cacheHit(String key, Class returnType) {
        String jsonStr = this.redisTemplate.opsForValue().get(key);
        if (jsonStr != null) {
            return JSON.parseObject(jsonStr, returnType);
        }
        return null;
    }
}

package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author ZQ
 * @create 2019-11-08 18:25
 */
@Service
public class IndexService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private RedissonClient redissonClient;
    private static final String KEY_PREFIX = "index:category";
    private static final String KEY_INDEX = "index:ALL";

    public List<CategoryEntity> queryLevel1Categlories() {
        String cache = this.redisTemplate.opsForValue().get(KEY_INDEX);
        if (StringUtils.isNotBlank(cache)) {
            return JSON.parseArray("cache", CategoryEntity.class);
        }

        Resp<List<CategoryEntity>> listResp = this.gmallPmsClient.queryCategories(1, null);
        List<CategoryEntity> categoryEntities = listResp.getData();
        this.redisTemplate.opsForValue().set(KEY_INDEX, JSON.toJSONString(categoryEntities));
        return listResp.getData();
    }


    public List<CategoryVo> queryCategoryVO(long pid) {
//         1、查询缓存，没有的话直接返回
        String cache = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(cache)) {
            return JSON.parseArray("cache", CategoryVo.class);
        }
//        2、如果缓存中没有，查询数据库
        Resp<List<CategoryVo>> resp = this.gmallPmsClient.queryCategoryWithSub(pid);
        List<CategoryVo> categoryVos = resp.getData();
//        3、查询完成后，放入缓存
        this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryVos));
        return resp.getData();
    }

    public String testLock() {
        RLock lock = redissonClient.getLock("lock");
        lock.lock();
//            获取到锁执行业务逻辑
        String numStr = this.redisTemplate.opsForValue().get("num");
        if (StringUtils.isEmpty(numStr)) {
            return null;
        }
        int num = Integer.parseInt(numStr);
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
//            释放锁
        lock.unlock();
        return "insert success";
    }

    public String testReadLock() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("lock");
        lock.readLock().lock(10L, TimeUnit.SECONDS);
        String msg = this.redisTemplate.opsForValue().get("msg");

//        lock.readLock().unlock();
        return msg;
    }

    public String testWriteLock() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("lock");
        lock.writeLock().lock(10L, TimeUnit.SECONDS);
        String msg = UUID.randomUUID().toString();
        this.redisTemplate.opsForValue().set("msg", msg);

//        lock.writeLock().unlock();
        return "数据写入成功" + msg;
    }
/*    public String testLock1() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
//        所有请求竞争锁
        if (lock) {
//            获取到锁执行业务逻辑
            String numStr = this.redisTemplate.opsForValue().get("num");
            if (StringUtils.isEmpty(numStr)) {
                return null;
            }
            int num = Integer.parseInt(numStr);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
//            释放锁
            this.redisTemplate.opsForValue().get("lock");
          *//*  if (org.apache.commons.lang3.StringUtils.equals(uuid, this.redisTemplate.opsForValue().get("key"))) {
                this.redisTemplate.delete("lock");
            }*//*
            Jedis jedis =null;
            try {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//            redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("key"), uuid);
                jedis = jedisPool.getResource();
                jedis.eval(script, Arrays.asList("lock"), Arrays.asList(uuid));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }

        } else {
//            没有获取到锁的请求进行重试
            try {
                TimeUnit.SECONDS.sleep(1);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return "insert success";
    }*/

}


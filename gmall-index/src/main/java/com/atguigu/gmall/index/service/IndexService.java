package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public synchronized String testLock() {
        String numStr = this.redisTemplate.opsForValue().get("num");
        if (StringUtils.isEmpty(numStr)){
            return null;
        }
        int num = Integer.parseInt(numStr);
        this.redisTemplate.opsForValue().set("num",String.valueOf(++num));
        return "insert success";
    }
}

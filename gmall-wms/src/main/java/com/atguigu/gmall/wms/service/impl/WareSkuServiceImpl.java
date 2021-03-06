package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public String checkAndLock(List<SkuLockVO> skuLockVOS) {
        //遍历
        skuLockVOS.forEach(skuLockVO -> {
            lockSku(skuLockVO);
        });
//查看有没有失败，有则回滚失败的记录
        List<SkuLockVO> success = skuLockVOS.stream().filter(skuLockVO -> skuLockVO.getLock()).collect(Collectors.toList());
        List<SkuLockVO> erro = skuLockVOS.stream().filter(skuLockVO -> !skuLockVO.getLock()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(erro)) {
            //回滚成功的记录
            success.forEach(skuLockVO -> {
                wareSkuDao.unlock(skuLockVO.getSkuWareId(), skuLockVO.getCount());
            });
            return "锁定失败:" + erro.stream().map(skuLockVO -> skuLockVO.getSkuId()).collect(Collectors.toList()).toString();
        }
        //保存锁定库存信息到redis中
        String orderToken = skuLockVOS.get(0).getOrderToken();
        this.redisTemplate.opsForValue().set("order:stock:"+skuLockVOS.get(0).getOrderToken(), JSON.toJSONString(skuLockVOS));
//发送延时消息，20分钟解锁库存
        this.amqpTemplate.convertAndSend("WMS-EXCHANGE", "wms.unlock", orderToken);
        return null;
    }

    private void lockSku(SkuLockVO skuLockVO) {
        RLock lock = this.redissonClient.getLock("sku:lock:" + skuLockVO.getSkuId());
        lock.lock();
        //验库存
        List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.checkStore(skuLockVO.getSkuId(), skuLockVO.getCount());
        skuLockVO.setLock(false);
        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
            //锁库存
            if (this.wareSkuDao.lock(wareSkuEntities.get(0).getId(), skuLockVO.getCount()) == 1) {
                skuLockVO.setLock(true);
                skuLockVO.setSkuWareId(wareSkuEntities.get(0).getId());
            }
        }

        lock.unlock();
    }

    //定时任务
   /* public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        scheduledExecutorService.schedule(()->{
            System.out.println("==========="+ LocalDateTime.now());
        },10, TimeUnit.SECONDS);
    }*/
}
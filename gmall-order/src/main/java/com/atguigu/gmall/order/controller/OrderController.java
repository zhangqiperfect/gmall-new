package com.atguigu.gmall.order.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.config.AlipayTemplate;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author ZQ
 * @create 2019-11-15 20:53
 */
@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private OrderService orderService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @GetMapping("confirm")
    public Resp<OrderConfirmVO> confirm() {
        OrderConfirmVO orderConfirmVO = this.orderService.confirm();
        return Resp.ok(orderConfirmVO);
    }

    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVO orderSubmitVO) {
        OrderEntity orderEntity = this.orderService.submit(orderSubmitVO);
        PayVo payVo = new PayVo();
        payVo.setBody("谷粒商城支付系统");
        payVo.setSubject("iPhone 11");
        payVo.setOut_trade_no(orderEntity.getOrderSn());
        payVo.setTotal_amount(orderEntity.getTotalAmount().toString());
        String form = null;
        try {
            form = this.alipayTemplate.pay(payVo);
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return Resp.ok(form);
    }

    @PostMapping("pay/success")
    public Resp<Object> paySuccess(PayAsyncVo payAsyncVo) {
        System.out.println("===============支付成功===================");
        //订单状态修改
        orderService.paySuccess(payAsyncVo.getOut_trade_no());
//        库存扣除
        return Resp.ok(null);
    }

    @RequestMapping("seckill/{skuId}")
    public Resp<Object> seckill(@PathVariable("skuId") Long skuId) throws InterruptedException {
//        查询秒杀库存
        String stockJson = this.redisTemplate.opsForValue().get("seckill:stock:" + skuId);
        if (StringUtils.isEmpty(stockJson)){
            return Resp.ok("秒杀不存在");
        }
        RSemaphore semaphore = redissonClient.getSemaphore("seckill:lock:" + skuId);
        Integer stock = Integer.valueOf(stockJson);
        semaphore.trySetPermits(stock);

        semaphore.acquire(1);

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("seckill:count:" + userInfo.getUserId());
        countDownLatch.trySetCount(1);

        SeckillVO seckillVO = new SeckillVO();
        seckillVO.setSkuId(skuId);
        seckillVO.setUserId(userInfo.getUserId());
        seckillVO.setCount(1);
        this.amqpTemplate.convertAndSend("SECKILL-EXCHANGE","seckill.create",seckillVO);

        countDownLatch.countDown();
        //更新redis数量
        this.redisTemplate.opsForValue().set("seckill:lock:" + skuId,String.valueOf(--stock));
        return  Resp.ok(null);
    }
    @GetMapping
    public Resp<Object> queryOrder() throws InterruptedException {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("seckill:count:" + userInfo.getUserId());
        countDownLatch.await();
        OrderEntity orderEntity= this.orderService.queryOrder();
        return Resp.ok(orderEntity);
    }

}

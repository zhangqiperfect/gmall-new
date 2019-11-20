package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.vo.CartItemVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.order.vo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.api.entity.MemberEntity;
import com.atguigu.gmall.ums.api.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author ZQ
 * @create 2019-11-15 12:43
 */
@Service
public class OrderService implements HandlerInterceptor {
    @Autowired
    private GmallUmsClient gmallUmsClient;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallCartClient gmallCartClient;
    @Autowired
    private GmallOmsClient gmallOmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;
    private static final String TOKEN_PREFIX = "order:token:";

    public OrderConfirmVO confirm() {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        UserInfo userInfo = LoginInterceptor.getUserInfo();
//        查询收货地址
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> addrResp = this.gmallUmsClient.queryAddressByUserId(userInfo.getUserId());
            orderConfirmVO.setAddresses(addrResp.getData());
        }, threadPoolExecutor);

//        获取购物车中的选中记录
        CompletableFuture<Void> cartFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<CartItemVo>> listResp = this.gmallCartClient.queryItemVO(userInfo.getUserId());
            List<CartItemVo> cartItemVos = listResp.getData();
            return cartItemVos;
        }, threadPoolExecutor).thenAcceptAsync(cartItemVos -> {
            if (CollectionUtils.isEmpty(cartItemVos)) {
                return;
            }
            //把购物车选中记录转化成订单商品列表
            List<OrderItemVO> orderItemVOS = null;
            orderItemVOS = cartItemVos.stream().map(cartItemVo -> {
                OrderItemVO orderItemVO = new OrderItemVO();
//            根据skuId查询sku
                Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(cartItemVo.getSkuId());
                SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
//            根据skuId查询销售属性
                Resp<List<SkuSaleAttrValueEntity>> skuSaleResp = this.gmallPmsClient.querySkuAttrBySkuId(cartItemVo.getSkuId());
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleResp.getData();
                orderItemVO.setSkuAttrValue(skuSaleAttrValueEntities);
                orderItemVO.setTitle(skuInfoEntity.getSkuTitle());
                orderItemVO.setSkuId(cartItemVo.getSkuId());
                orderItemVO.setPrice(skuInfoEntity.getPrice());
                orderItemVO.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                orderItemVO.setCount(cartItemVo.getCount());
//            根据skuId获取营销信息
                Resp<List<ItemSaleVo>> saleResp = this.gmallSmsClient.queryItemSaleVOs(cartItemVo.getSkuId());
                List<ItemSaleVo> itemSaleVos = saleResp.getData();
                orderItemVO.setSales(itemSaleVos);
//            查询库存信息
                Resp<List<WareSkuEntity>> storeResp = this.gmallWmsClient.queryWareBySkuId(cartItemVo.getSkuId());
                List<WareSkuEntity> wareSkuEntities = storeResp.getData();
                orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                orderItemVO.setWeight(skuInfoEntity.getWeight());
                return orderItemVO;
            }).collect(Collectors.toList());
            orderConfirmVO.setOrderItems(orderItemVOS);
        }, threadPoolExecutor);
        CompletableFuture<Void> boundFuture = CompletableFuture.runAsync(() -> {
            //        获取用户信息
            Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryUserById(userInfo.getUserId());
            MemberEntity memberEntity = memberEntityResp.getData();
            orderConfirmVO.setBounds(memberEntity.getIntegration());
        }, threadPoolExecutor);
        CompletableFuture<Void> tokenFuture = CompletableFuture.runAsync(() -> {
            //        获取唯一标志防止重复提交并作为订单号
            String timeId = IdWorker.getTimeId();
            orderConfirmVO.setOrderToken(timeId);
            this.redisTemplate.opsForValue().set(TOKEN_PREFIX + timeId, timeId);
        }, threadPoolExecutor);
        CompletableFuture.allOf(tokenFuture, addressFuture, cartFuture, boundFuture).join();
        return orderConfirmVO;
    }

    public OrderEntity submit(OrderSubmitVO orderSubmitVO) {
//        1. 验证令牌防止重复提交
        String orderToken = orderSubmitVO.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(TOKEN_PREFIX + orderToken), orderToken);
        if (flag == 0L) {
            throw new RuntimeException("请不要重复提交");
        }
//        2. 验证价格
        BigDecimal totalPrice = orderSubmitVO.getTotalPrice();
        List<OrderItemVO> orderItemVOS = orderSubmitVO.getOrderItemVOS();
        if (CollectionUtils.isEmpty(orderItemVOS)) {
            throw new RuntimeException("请添加购物清单");
        }
        BigDecimal currentPrice = orderItemVOS.stream().map(orderItemVO -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(orderItemVO.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            return skuInfoEntity.getPrice().multiply(new BigDecimal(orderItemVO.getCount()));
        }).reduce((a, b) -> a.add(b)).get();
        if (totalPrice.compareTo(currentPrice) != 0) {
            throw new RuntimeException("请刷新页面后重试");
        }

//        3. 验证库存，并锁定库存
        List<SkuLockVO> skuLockVOS = orderItemVOS.stream().map(orderItemVO -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(orderItemVO.getSkuId());
            skuLockVO.setCount(orderItemVO.getCount());
            skuLockVO.setOrderToken(orderToken);
            return skuLockVO;
        }).collect(Collectors.toList());
        Resp<Object> objectResp = this.gmallWmsClient.checkAndLock(skuLockVOS);
        if (objectResp.getCode() == 1) {
            throw new RuntimeException(objectResp.getMsg());
        }

//        4. 生成订单
        UserInfo userInfo = null;
        Resp<OrderEntity> orderResp = null;
        try {
            userInfo = LoginInterceptor.getUserInfo();
            orderSubmitVO.setId(userInfo.getUserId());
            Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryUserById(userInfo.getUserId());
            MemberEntity memberEntity = memberEntityResp.getData();
            orderSubmitVO.setUserName(memberEntity.getUsername());
            orderResp = this.gmallOmsClient.createOrder(orderSubmitVO);
        } catch (Exception e) {
            e.printStackTrace();
            // this.amqpTemplate.convertAndSend("WMS-EXCHANGE","wms.ttl",orderToken);
            throw new RuntimeException("订单创建失败，服务器异常");
        }
//        5. 删购物车中对应的记录（消息队列）
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userInfo.getUserId());
        List<Long> skuIds = orderItemVOS.stream().map(orderItemVO -> orderItemVO.getSkuId()).collect(Collectors.toList());
        map.put("skuIds", skuIds);
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE", "cart.delete", map);
        if (orderResp != null) {
            return orderResp.getData();
        }
        return null;
    }

    public void paySuccess(String out_trade_no) {
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","order.pay",out_trade_no);
    }

    public OrderEntity queryOrder() {
        return null;
    }
}

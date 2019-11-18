package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.cart.vo.CartItemVo;
import com.atguigu.gmall.cart.vo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ZQ
 * @create 2019-11-13 18:52
 */
@Service
public class CartService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "cart:key:";
    private static final String CURRENT_PRICE_PREFIX = "cart:price:";

    public void addCart(Cart cart) {
        Integer count = cart.getCount();
        String key = getKey();
        //判断是否已添加该物品
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        String skuId = cart.getSkuId().toString();
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            //有更新数量
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount() + count);
        } else {
            //没有则新增记录
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            cart.setTitle(skuInfoEntity.getSkuTitle());
            cart.setCheck(true);
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            //查询营销属性
            Resp<List<ItemSaleVo>> listResp = this.gmallSmsClient.queryItemSaleVOs(cart.getSkuId());
            cart.setSales(listResp.getData());
            //查询销售属性
            Resp<List<SkuSaleAttrValueEntity>> listResp1 = this.gmallPmsClient.querySkuAttrBySkuId(cart.getSkuId());
            cart.setSkuAttrValue(listResp1.getData());
            cart.setPrice(skuInfoEntity.getPrice());
            this.redisTemplate.opsForValue().set(CURRENT_PRICE_PREFIX + skuId, skuInfoEntity.getPrice().toString());
        }
        //            同步到reidis中
        hashOps.put(skuId, JSON.toJSONString(cart));
    }

    public List<Cart> queryCarts() {
        //判断登录状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key1 = KEY_PREFIX + userInfo.getUserKey();
        //查询未登录状态购物车
        BoundHashOperations<String, Object, Object> userKeyOps = this.redisTemplate.boundHashOps(key1);
        List<Object> cartJsonList = userKeyOps.values();
        List<Cart> userKeyCarts = null;
        if (!CollectionUtils.isEmpty(cartJsonList)) {
            userKeyCarts = cartJsonList.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }
//        判断未登录直接返回
        if (userInfo.getUserId() == null) {
            return userKeyCarts;
        }
//        已登录，查询登录的购物车
        String key2 = KEY_PREFIX + userInfo.getUserId();
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key2);
//        查询未登录状态购物车是否为空
        if (!CollectionUtils.isEmpty(userKeyCarts)) {
//        未登录购物车不为空则合并
            userKeyCarts.forEach(cart -> {
                if (userIdOps.hasKey(cart.getSkuId().toString())) {
                    String cartJson = userIdOps.get(cart.getSkuId().toString()).toString();
                    Cart idCart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(idCart.getCount() + cart.getCount());
                    userIdOps.put(cart.getSkuId().toString(), JSON.toJSONString(idCart));
                } else {
                    userIdOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
                }
            });
        }
//        未登录购物车为空则查询登录购物车直接返回
        List<Object> userIdJsonList = userIdOps.values();
        if (CollectionUtils.isEmpty(userIdJsonList)) {
            return null;
        }
        List<Cart> userIdCarts = userIdJsonList.stream().map(userIdJson -> {
            Cart cart = JSON.parseObject(userIdJson.toString(), Cart.class);
            cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PREFIX + cart.getSkuId())));
            return cart;
        }).collect(Collectors.toList());
        return userIdCarts;
    }

    private String getKey() {
        String key = KEY_PREFIX;
        //判断是否登录
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != null) {
            key += userInfo.getUserId();
        } else {
            key += userInfo.getUserKey();
        }
        return key;
    }

    public void updateCart(Cart cart) {
        String key = getKey();
        Integer count = cart.getCount();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(cart.getSkuId().toString())) {
//             获取购物车中要修改数量的购物车记录
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }


    }

    public void deleteCart(Long skuId) {
        String key = getKey();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            hashOps.delete(skuId.toString());
        }
    }


    public void checkCart(List<Cart> carts) {
        String key = getKey();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        carts.forEach(cart -> {
            Boolean check = cart.getCheck();
            if (hashOps.hasKey(cart.getSkuId().toString())) {
//             获取购物车中要修改数量的购物车记录
                String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
                cart = JSON.parseObject(cartJson, Cart.class);
                cart.setCheck(check);
                hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            }
        });
    }

    public List<CartItemVo> queryItemVO(Long userId) {

//        已登录，查询登录的购物车
        String key2 = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key2);

//        未登录购物车为空则查询登录购物车直接返回
        List<Object> userIdJsonList = userIdOps.values();
        if (CollectionUtils.isEmpty(userIdJsonList)) {
            return null;
        }
        //获取所有购物车记录
        List<CartItemVo> cartItemVos = userIdJsonList.stream().map(userIdJson -> {
            Cart cart = JSON.parseObject(userIdJson.toString(), Cart.class);
            cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PREFIX + cart.getSkuId())));
            return cart;
        }).filter(cart -> cart.getCheck()).map(cart -> {
            CartItemVo cartItemVo = new CartItemVo();
            cartItemVo.setCount(cart.getCount());
            cartItemVo.setSkuId(cart.getSkuId());
            return cartItemVo;
        }).collect(Collectors.toList());
        return cartItemVos;
    }
}

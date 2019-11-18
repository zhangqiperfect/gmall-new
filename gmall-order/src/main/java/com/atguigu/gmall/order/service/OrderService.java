package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.vo.CartItemVo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.order.vo.OrderItemVO;
import com.atguigu.gmall.order.vo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.api.entity.MemberEntity;
import com.atguigu.gmall.ums.api.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
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
    private ThreadPoolExecutor threadPoolExecutor;

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
            orderItemVOS= cartItemVos.stream().map(cartItemVo -> {
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
        }, threadPoolExecutor);
        CompletableFuture.allOf(tokenFuture, addressFuture, cartFuture, boundFuture).join();
        return orderConfirmVO;
    }
}

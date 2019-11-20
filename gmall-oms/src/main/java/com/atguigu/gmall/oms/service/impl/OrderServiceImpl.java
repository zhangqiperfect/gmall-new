package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.dao.OrderItemDao;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsClient;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.ums.api.entity.MemberReceiveAddressEntity;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private OrderDao orderDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public OrderEntity createOrder(OrderSubmitVO orderSubmitVO) {
        //新增订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSubmitVO.getOrderToken());
        orderEntity.setMemberId(orderSubmitVO.getId());
        orderEntity.setTotalAmount(orderSubmitVO.getTotalPrice());
        orderEntity.setPayType(orderSubmitVO.getPayType());
        orderEntity.setCreateTime(new Date());
        orderEntity.setSourceType(1);
        orderEntity.setStatus(0);
        orderEntity.setDeliveryCompany(orderSubmitVO.getDeliveryCompany());
        orderEntity.setAutoConfirmDay(15);
        orderEntity.setModifyTime(orderEntity.getCreateTime());
        orderEntity.setConfirmStatus(0);
        orderEntity.setDeleteStatus(0);
        //根据订单明细查询营销信息获取成长积分和赠送积分
        orderEntity.setGrowth(100);
        orderEntity.setIntegration(200);
        orderEntity.setMemberUsername(orderSubmitVO.getUserName());
        //查询营销信息 店铺 spu sku 品类
        MemberReceiveAddressEntity address = orderSubmitVO.getAddressEntity();
        if (address != null) {
            orderEntity.setReceiverCity(address.getCity());
            orderEntity.setReceiverDetailAddress(address.getDetailAddress());
            orderEntity.setReceiverName(address.getName());
            orderEntity.setReceiverPhone(address.getPhone());
            orderEntity.setReceiverPostCode(address.getPostCode());
            orderEntity.setReceiverProvince(address.getProvince());
            orderEntity.setReceiverRegion(address.getRegion());
        }

        this.save(orderEntity);

        //新增订单详情单
        List<OrderItemVO> orderItemVOS = orderSubmitVO.getOrderItemVOS();
        if (!CollectionUtils.isEmpty(orderItemVOS)) {
            orderItemVOS.forEach(orderItemVO -> {
                //远程调用pms查询spu
                Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(orderItemVO.getSkuId());
                SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                OrderItemEntity itemEntity = new OrderItemEntity();
                itemEntity.setSkuQuantity(orderItemVO.getCount());
                itemEntity.setSkuPic(skuInfoEntity.getSkuDefaultImg());
                itemEntity.setSkuName(skuInfoEntity.getSkuTitle());
                itemEntity.setSkuId(orderItemVO.getSkuId());
                itemEntity.setSpuId(skuInfoEntity.getSpuId());
                itemEntity.setOrderSn(orderSubmitVO.getOrderToken());
                itemEntity.setOrderId(orderEntity.getId());
                itemEntity.setCategoryId(skuInfoEntity.getCatalogId());
                itemEntity.setSkuAttrsVals(JSON.toJSONString(orderItemVO.getSkuAttrValue()));
                itemEntity.setSkuPrice(skuInfoEntity.getPrice());
                this.orderItemDao.insert(itemEntity);

            });
        }
        this.amqpTemplate.convertAndSend("OMS-EXCHANGE","oms.close",orderSubmitVO.getOrderToken());
        return orderEntity;
    }

    @Override
    public int closeOrder(String orderToken) {
    return this.orderDao.closeOrder(orderToken);
    }

    @Override
    public int success(String orderToken) {

    return orderDao.success(orderToken);
    }

}
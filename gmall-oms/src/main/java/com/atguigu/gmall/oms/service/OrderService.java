package com.atguigu.gmall.oms.service;

import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 订单
 *
 * @author qinhan
 * @email 1589125792@qq.com
 * @date 2019-10-28 20:48:05
 */
public interface OrderService extends IService<OrderEntity> {

    PageVo queryPage(QueryCondition params);

    OrderEntity createOrder(OrderSubmitVO orderSubmitVO);

    int closeOrder(String orderToken);

    int success(String orderToken);
}


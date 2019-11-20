package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author qinhan
 * @email 1589125792@qq.com
 * @date 2019-10-28 20:48:05
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	public int closeOrder(String orderToken);

    int success(String orderToken);
}

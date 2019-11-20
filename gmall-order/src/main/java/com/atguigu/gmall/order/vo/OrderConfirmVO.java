package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.ums.api.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * @author ZQ
 * @create 2019-11-15 16:46
 */
@Data
public class OrderConfirmVO {
    //收货地址 ums_member_receive_address表
    private List<MemberReceiveAddressEntity> addresses;
    //购物车清单，根据购物车页面传递过来的skuId查询
    private List<OrderItemVO> orderItems;//订单商品清单
    //可用积分，ums_member表中的integration字段
    private Integer bounds;
    //订单令牌，防止重复提交
    private String orderToken;
}

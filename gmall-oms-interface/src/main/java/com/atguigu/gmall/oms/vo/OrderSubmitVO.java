package com.atguigu.gmall.oms.vo;


import com.atguigu.gmall.ums.api.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ZQ
 * @create 2019-11-17 20:01
 */
@Data
public class OrderSubmitVO {
//    用户名
    private String userName;
//    用户id
    private Long id;
//    收货地址
    private MemberReceiveAddressEntity addressEntity;
//    支付方式
    private Integer payType;
//   物流公司
    private  String deliveryCompany;
//    订货清单
    private List<OrderItemVO> orderItemVOS;
//    下单时使用的积分
    private Integer useItergration;
//    订单总价,用于验价
    private BigDecimal totalPrice;
//     用于防重和作为订单编号
    private String orderToken;
}

package com.atguigu.gmall.order.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.config.AlipayTemplate;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.order.vo.PayAsyncVo;
import com.atguigu.gmall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
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
    @GetMapping("confirm")
    public Resp<OrderConfirmVO> confirm(){
        OrderConfirmVO orderConfirmVO= this.orderService.confirm();
        return Resp.ok(orderConfirmVO);
    }
    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVO orderSubmitVO){
        OrderEntity orderEntity = this.orderService.submit(orderSubmitVO);
        PayVo payVo = new PayVo();
        payVo.setBody("谷粒商城支付系统");
        payVo.setSubject("iPhone 11");
        payVo.setOut_trade_no(orderEntity.getOrderSn());
        payVo.setTotal_amount(orderEntity.getTotalAmount().toString());
        String form=null;
        try {
             form = this.alipayTemplate.pay(payVo);
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return Resp.ok(form);
    }
    @PostMapping("pay/success")
    public Resp<Object> paySuccess(PayAsyncVo payAsyncVo){
        System.out.println("===============支付成功===================");
        //订单状态修改
        orderService.paySuccess(payAsyncVo.getOut_trade_no());
//        库存扣除
        return Resp.ok(null);
    }
}

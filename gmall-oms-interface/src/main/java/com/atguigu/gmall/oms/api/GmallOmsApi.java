package com.atguigu.gmall.oms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author ZQ
 * @create 2019-11-18 18:49
 */
public interface GmallOmsApi {
    @PostMapping("oms/order")
    public Resp<OrderEntity> createOrder(@RequestBody OrderSubmitVO orderSubmitVO);
}

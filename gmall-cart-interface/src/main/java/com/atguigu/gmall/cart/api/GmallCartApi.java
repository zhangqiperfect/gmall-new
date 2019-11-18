package com.atguigu.gmall.cart.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.vo.CartItemVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author ZQ
 * @create 2019-11-15 22:28
 */
public interface GmallCartApi {
    @GetMapping("cart/order/{userId}")
    public Resp<List<CartItemVo>> queryItemVO(@PathVariable("userId")Long userId);
}

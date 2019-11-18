package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-15 20:58
 */
@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {
}

package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-13 18:47
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}

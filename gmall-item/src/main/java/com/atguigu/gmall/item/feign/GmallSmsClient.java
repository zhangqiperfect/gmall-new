package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-10 15:52
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}

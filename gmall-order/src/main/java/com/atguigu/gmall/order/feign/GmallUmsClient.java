package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-15 20:58
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}

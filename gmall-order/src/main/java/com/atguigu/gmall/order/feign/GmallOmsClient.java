package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-15 20:58
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}

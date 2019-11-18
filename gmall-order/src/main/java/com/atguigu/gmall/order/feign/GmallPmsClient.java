package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-15 20:58
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}

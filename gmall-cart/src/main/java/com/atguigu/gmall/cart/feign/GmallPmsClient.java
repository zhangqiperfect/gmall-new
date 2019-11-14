package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-13 18:45
 */
@FeignClient("pms-service")
public interface GmallPmsClient  extends GmallPmsApi {
}

package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-12 16:18
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}

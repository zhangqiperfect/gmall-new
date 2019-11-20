package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-18 18:27
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}

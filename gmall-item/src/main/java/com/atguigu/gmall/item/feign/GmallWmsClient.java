package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-10 15:52
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}

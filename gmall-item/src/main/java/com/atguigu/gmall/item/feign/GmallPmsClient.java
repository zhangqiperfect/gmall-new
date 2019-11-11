package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-10 15:52
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}

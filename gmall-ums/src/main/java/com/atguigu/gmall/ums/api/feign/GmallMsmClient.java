package com.atguigu.gmall.ums.api.feign;

import com.atguigu.gmall.msm.api.GmallMsmApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author ZQ
 * @create 2019-11-14 11:32
 */
@FeignClient("msm-service")
public interface GmallMsmClient extends GmallMsmApi {
}

package com.atguigu.gmall.msm.api;

import com.atguigu.core.bean.Resp;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author ZQ
 * @create 2019-11-14 14:06
 */
public interface GmallMsmApi {
    @PostMapping("user/sendCode")
    public Resp<Object> sendCode(@RequestParam("phoneNo") String phoneNo);
}

package com.atguigu.gmall.wms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallWmsApi {

    //根据skuId查询库存
    @GetMapping("wms/waresku/{skuId}")
    public Resp<List<WareSkuEntity>> queryWareBySkuId(@PathVariable("skuId")Long skuId);
//    锁库存
    @PostMapping("wms/waresku/check/lock")
    public Resp<Object> checkAndLock(@RequestBody List<SkuLockVO> skuLockVOS);
}

package com.atguigu.gmall.wms.vo;

import lombok.Data;

/**
 * @author ZQ
 * @create 2019-11-17 20:25
 */
@Data
public class SkuLockVO {
    private Long skuId;
    private Integer count;
    private Boolean lock;//锁定成功即为true
    private Long skuWareId;//锁定库存的id

}

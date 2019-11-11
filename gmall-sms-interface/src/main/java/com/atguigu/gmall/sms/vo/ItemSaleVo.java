package com.atguigu.gmall.sms.vo;

import lombok.Data;

/**
 * @author ZQ
 * @create 2019-11-10 13:34
 */
@Data
public class ItemSaleVo {
    private String type;  //满减， 打折，积分
    private String desc; //优惠信息的具体描述

}

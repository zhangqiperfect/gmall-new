package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ZQ
 * @create 2019-11-15 16:46
 */
@Data
public class OrderItemVO {
    private Long skuId;//商品id
    private String title;//标题
    private String defaultImage;//图片
    private BigDecimal price; //加入购物车时的价格
    private Integer count ;//购买数量
    private Boolean store;//库存
    private List<SkuSaleAttrValueEntity>skuAttrValue;//销售属性
    private List<ItemSaleVo> sales;//营销属性
    private BigDecimal weight;//重量
}

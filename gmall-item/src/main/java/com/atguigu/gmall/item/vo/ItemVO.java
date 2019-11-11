package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.util.List;

/**
 * @author ZQ
 * @create 2019-11-10 13:30
 */
@Data
public class ItemVO extends SkuInfoEntity {
    private BrandEntity brand;
    private CategoryEntity category;
    private SpuInfoEntity spuInfoEntity;
    private List<String> pics;//sku图片列表
    private List<ItemSaleVo> sales;//营销信息
    private boolean store;//是否有货
    private List<SkuSaleAttrValueEntity> skuSales;//spu下所有sku信息
    private SpuInfoDescEntity desc;//描述信息
    private List<GroupVO> groups;//组及组下的规格属性及值

}

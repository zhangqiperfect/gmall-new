package com.atguigu.gmall.item.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ZQ
 * @create 2019-11-10 13:51
 */
@Service
public class ItemService {
    @Autowired
    private GmallPmsApi gmallPmsApi;
    @Autowired
    private GmallWmsApi gmallWmsApi;
    @Autowired
    private GmallSmsApi gmallSmsApi;

    public ItemVO item(long skuId) {
        ItemVO itemVO = new ItemVO();
        //1、查询sku信息
        Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsApi.querySkuById(skuId);
        SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
        Long spuId = skuInfoEntity.getSpuId();
        BeanUtils.copyProperties(skuInfoEntity, itemVO);
//        2.品牌
        Resp<BrandEntity> brandEntityResp = this.gmallPmsApi.queryBrandById(skuInfoEntity.getBrandId());
        itemVO.setBrand(brandEntityResp.getData());
//        3.分类
        Resp<CategoryEntity> categoryEntityResp = this.gmallPmsApi.queryCategoryById(skuInfoEntity.getCatalogId());
        itemVO.setCategory(categoryEntityResp.getData());
//        4.spu
        Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsApi.querySpuById(spuId);
        itemVO.setSpuInfoEntity(spuInfoEntityResp.getData());
//       5. 设置图片信息
        Resp<List<String>> listResp = this.gmallPmsApi.queryPicsById(skuId);
        itemVO.setPics(listResp.getData());
//        6.设置营销
        Resp<List<ItemSaleVo>> listResp1 = this.gmallSmsApi.queryItemSaleVOs(skuId);
        itemVO.setSales(listResp1.getData());
//        7.设置库存
        Resp<List<WareSkuEntity>> listResp2 = this.gmallWmsApi.queryWareBySkuId(skuId);
        List<WareSkuEntity> wareSkuEntities = listResp2.getData();
        itemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
//        8.设置spu所有的销售属性
        Resp<List<SkuSaleAttrValueEntity>> listResp3 = this.gmallPmsApi.querySaleAttrValues(spuId);
        itemVO.setSkuSales(listResp3.getData());
//        9.spu的描述信息
        Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsApi.querySpuDesc(spuId);
        itemVO.setDesc(spuInfoDescEntityResp.getData());
//        10.设置规格属性分组下的规格参数及值
        Resp<List<GroupVO>> listResp4 = this.gmallPmsApi.queryGroupVOByCid(skuInfoEntity.getCatalogId(), spuId);
        itemVO.setGroups(listResp4.getData());
        return itemVO;
    }
}

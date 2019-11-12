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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

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
    @Autowired
    private  ThreadPoolExecutor threadPoolExecutor;

    public ItemVO item(long skuId) {
        ItemVO itemVO = new ItemVO();
        CompletableFuture<SkuInfoEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //1、查询sku信息
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsApi.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            BeanUtils.copyProperties(skuInfoEntity, itemVO);
            return skuInfoEntity;
        },threadPoolExecutor);
        CompletableFuture<Void> brandFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
//        2.品牌
            Resp<BrandEntity> brandEntityResp = this.gmallPmsApi.queryBrandById(skuInfoEntity.getBrandId());
            itemVO.setBrand(brandEntityResp.getData());
        },threadPoolExecutor);

        CompletableFuture<Void> categoryFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
//        3.分类
            Resp<CategoryEntity> categoryEntityResp = this.gmallPmsApi.queryCategoryById(skuInfoEntity.getCatalogId());
            itemVO.setCategory(categoryEntityResp.getData());
        },threadPoolExecutor);

        CompletableFuture<Void> spuFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
//        4.spu
            Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsApi.querySpuById(skuInfoEntity.getSpuId());
            itemVO.setSpuInfoEntity(spuInfoEntityResp.getData());
        },threadPoolExecutor);


        CompletableFuture<Void> picFuture = CompletableFuture.runAsync(() -> {
//       5. 设置图片信息
            Resp<List<String>> listResp = this.gmallPmsApi.queryPicsById(skuId);
            itemVO.setPics(listResp.getData());
        },threadPoolExecutor);

        CompletableFuture<Void> saleFuture = CompletableFuture.runAsync(() -> {
//        6.设置营销
            Resp<List<ItemSaleVo>> listResp1 = this.gmallSmsApi.queryItemSaleVOs(skuId);
            itemVO.setSales(listResp1.getData());
        },threadPoolExecutor);


        CompletableFuture<Void> storeFuture = CompletableFuture.runAsync(() -> {
//        7.设置库存
            Resp<List<WareSkuEntity>> listResp2 = this.gmallWmsApi.queryWareBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = listResp2.getData();
            itemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
        },threadPoolExecutor);

        CompletableFuture<Void> spusaleFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
//        8.设置spu所有的销售属性
            Resp<List<SkuSaleAttrValueEntity>> listResp3 = this.gmallPmsApi.querySaleAttrValues(skuInfoEntity.getSpuId());
            itemVO.setSkuSales(listResp3.getData());
        },threadPoolExecutor);

        CompletableFuture<Void> descFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
//        9.spu的描述信息
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsApi.querySpuDesc(skuInfoEntity.getSpuId());
            itemVO.setDesc(spuInfoDescEntityResp.getData());
        },threadPoolExecutor);

        CompletableFuture<Void> groupFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
//        10.设置规格属性分组下的规格参数及值
            Resp<List<GroupVO>> listResp4 = this.gmallPmsApi.queryGroupVOByCid(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
            itemVO.setGroups(listResp4.getData());
        },threadPoolExecutor);

        CompletableFuture.allOf(groupFuture,descFuture,spusaleFuture,storeFuture,saleFuture,picFuture,spuFuture,categoryFuture,brandFuture).join();
        return itemVO;
    }

}

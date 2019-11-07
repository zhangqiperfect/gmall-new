package com.atguigu.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author ZQ
 * @create 2019-11-07 18:52
 */
@Component
public class ItemListenner {
    @Autowired
    private JestClient jestClient;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "GMALL-SEARCH-QUEUE", durable = "true"),
            exchange = @Exchange(name = "GMALL-ITEM-EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true", durable = "true"),
            key = {"item.*"}
    ))
    public void listen(Map<String, Object> map) {
        if (CollectionUtils.isEmpty(map)) {
            return;
        }
        String type = map.get("type").toString();
        long spuId = (long) map.get("spuId");
        System.out.println(map);
        if (StringUtils.equals("insert", type) || StringUtils.equals("update", type)) {

            Resp<List<SkuInfoEntity>> skuResp = this.gmallPmsClient.querySkuBySpuId(spuId);
            List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
            if (CollectionUtils.isEmpty(skuInfoEntities)) {
                return;
            }
            skuInfoEntities.forEach(skuInfoEntity -> {
                GoodsVO goodsVO = new GoodsVO();


                //设置sku相关的数据
                goodsVO.setName(skuInfoEntity.getSkuTitle());
                goodsVO.setId(skuInfoEntity.getSkuId());
                goodsVO.setPic(skuInfoEntity.getSkuDefaultImg());
                goodsVO.setPrice(skuInfoEntity.getPrice());
                goodsVO.setSale(100);  //销量
                goodsVO.setSort(0);  //综合排序

                //设置品牌相关的
                Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResp.getData();
                if (brandEntity != null) {
                    goodsVO.setBrandId(skuInfoEntity.getBrandId());
                    goodsVO.setBrandName(brandEntity.getName());
                }


                //设置分类相关的
                Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
                CategoryEntity categoryEntity = categoryEntityResp.getData();
                if (categoryEntity != null) {
                    goodsVO.setProductCategoryId(skuInfoEntity.getCatalogId());
                    goodsVO.setProductCategoryName(categoryEntity.getName());

                }

                //设置搜索相关的
                Resp<List<SpuAttributeValueVO>> searchAttrValueResp = this.gmallPmsClient.querySearchAttrValue(spuId);
                List<SpuAttributeValueVO> spuAttributeValueVOList = searchAttrValueResp.getData();
                goodsVO.setAttrValueList(spuAttributeValueVOList);

                //库存
                Resp<List<WareSkuEntity>> resp = this.gmallWmsClient.queryWareBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = resp.getData();
                if (wareSkuEntities.stream().anyMatch(t -> t.getStock() > 0)) {
                    goodsVO.setStock(1L);
                } else {
                    goodsVO.setStock(0L);
                }
                goodsVO.setStock(null);

                Index index = new Index.Builder(goodsVO).index("goods").type("info").id(skuInfoEntity.getSkuId().toString()).build();
                try {
                    this.jestClient.execute(index);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } else if (StringUtils.equals("delete", type)) {

        }

    }


}

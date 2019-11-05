package com.atguigu.gmall.search;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private JestClient jestClient;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;


    @Test
    public void importData(){
        Long pageNum = 1L;
        Long pageSize = 100L;
        do {
            //分页查询spu
            QueryCondition condition = new QueryCondition();
            condition.setPage(pageNum);
            condition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> listResp = this.gmallPmsClient.querySpuPage(condition);
            //获取当前页的spuInfo数据
            List<SpuInfoEntity> spuInfoEntities= listResp.getData();


            //遍历spu获取spu下的所有sku导入到索引库中
            for (SpuInfoEntity spuInfoEntity : spuInfoEntities) {
                Resp<List<SkuInfoEntity>> skuResp = this.gmallPmsClient.querySkuBySpuId(spuInfoEntity.getId());
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
                if(CollectionUtils.isEmpty(skuInfoEntities)){
                    continue;
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
                    Resp<List<SpuAttributeValueVO>> searchAttrValueResp = this.gmallPmsClient.querySearchAttrValue(spuInfoEntity.getId());
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

            }

            pageSize = Long.valueOf(spuInfoEntities.size());//获取当前页的记录数
            pageNum++;//下一页
        }while (pageSize == 100); // 循环条件

    }























    @Test
    public void create() throws IOException {
        //新增数据
//        User user = new User("zhang3", "123456", 23);
//        Index index = new Index.Builder(user).index("user").type("info").id("1").build();
//        jestClient.execute(index);

        //更新数据
//        User user = new User("张三", null, null);
////        Map<String, User> map = new HashMap<>();
////        map.put("doc",user);
////        Update update = new Update.Builder(map).index("user").type("info").id("1").build();
////        DocumentResult result = jestClient.execute(update);
////        System.out.println(result.toString());

        //查询数据
//        String query="{\n" +
//                "  \"query\": {\n" +
//                "    \"match_all\": {}\n" +
//                "  }\n" +
//                "}";
//        Search search = new Search.Builder(query).addIndex("user").addType("info").build();
//        SearchResult result = jestClient.execute(search);
//        System.out.println(result.toString());
//        //两种获取查询结果集的方式
//        System.out.println(result.getSourceAsObjectList(User.class, false));
//
//        result.getHits(User.class).forEach(hit->{
//            System.out.println(hit.source);
//        });

//        Get get = new Get.Builder("user", "1").build();
//        System.out.println(jestClient.execute(get));

        //删除数据
        Delete delete = new Delete.Builder("1").index("user").type("info").build();
        DocumentResult result = jestClient.execute(delete);
        System.out.println(result.toString());
    }










    @Test
    void contextLoads() {
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class User{
    private String name;
    private String password;
    private Integer age;
}

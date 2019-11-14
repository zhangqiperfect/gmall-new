package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
    //根据skuId查询销售属性
    @GetMapping("pms/skusaleattrvalue/sku/{skuId}")
    public Resp<List<SkuSaleAttrValueEntity>> querySkuAttrBySkuId(@PathVariable("skuId") Long skuId);
    //查询分组信息及值
    @GetMapping("pms/attrgroup/item/group/{cid}/{spuId}")
    public Resp<List<GroupVO>> queryGroupVOByCid(@PathVariable("cid") long cid, @PathVariable("spuId")long spuId);
    //查询spu详情
    @GetMapping("pms/spuinfodesc/info/{spuId}")
    public Resp<SpuInfoDescEntity> querySpuDesc(@PathVariable("spuId") Long spuId);
    //查询spu下的sku的销售属性
    @GetMapping("pms/skusaleattrvalue/{spuId}")
    public Resp<List<SkuSaleAttrValueEntity>> querySaleAttrValues(@PathVariable("spuId")long spuId);
    //查询sku图片
    @GetMapping("pms/skuimages/{skuId}")
    public Resp<List<String>> queryPicsById(@PathVariable("skuId") long skuId);
    //根据skuId查询spu
    @GetMapping("pms/spuinfo/info/{id}")
    public Resp<SpuInfoEntity> querySpuById(@PathVariable("id") Long id);
    //根据skuId查询skuinfo
    @GetMapping("pms/skuinfo/info/{skuId}")
    public Resp<SkuInfoEntity> querySkuById(@PathVariable("skuId") Long skuId);
    //分页查询spu
    @PostMapping("pms/spuinfo/list")
    public Resp<List<SpuInfoEntity>> querySpuPage(@RequestBody QueryCondition queryCondition);

    //根据spuId查询sku
    @GetMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> querySkuBySpuId(@PathVariable("spuId")Long spuId);

    //根据brandsId查询品牌
    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);

    //根据categoryId查询分类
    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

//    查询一级分类

    @GetMapping("pms/category")
    public Resp<List<CategoryEntity>>  queryCategories(@RequestParam(value = "level",defaultValue = "0")Integer level,
                                                       @RequestParam(value = "parentCid",required = false)Long parentCid);
    //根据spuId查询检索属性
    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<SpuAttributeValueVO>> querySearchAttrValue(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/category/{pid}")
    public Resp<List<CategoryVo>> queryCategoryWithSub(@PathVariable("pid") long pid);

}

package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.SpuInfoDescService;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.gmall.sms.vo.SaleVO;

import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {


    @Autowired
    private SpuInfoDescDao descDao;
    @Autowired
    private ProductAttrValueDao productAttrValueDao;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesDao skuImagesDao;
    @Autowired
    private SkuSaleAttrValueDao saleAttrValueDao;
    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuInfoByKeyPage(Long catId, QueryCondition condition) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        //判断catId是否为0
        if(catId != 0){
            wrapper.eq("catalog_id",catId);
        }

        //判断key是否为空
        String key = condition.getKey();
        if(StringUtils.isNoneBlank(key)){
            wrapper.and(t->t.eq("id",key).or().like("spu_name",key));
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );
        return new PageVo(page);
    }


    @GlobalTransactional
    @Override
    public void bigSave(SpuInfoVO spuInfoVO) {

        //新增spu相关的3张表
        //1.1 新增spuInfo
        Long spuId = saveSpuInfo(spuInfoVO);

        //1.2 新增spuInfoDesc
//        saveSpuDesc(spuInfoVO, spuId);
        this.spuInfoDescService.saveSpuDesc(spuInfoVO,spuId);


        //1.3 新增基本属性productAttrValue
        saveBaseAttr(spuInfoVO, spuId);

        //新增sku相关的3张表
        saveSku(spuInfoVO, spuId);

//        int i = 1 / 0;


    }

    private void saveSku(SpuInfoVO spuInfoVO, Long spuId) {
        List<SkuInfoVO> skus = spuInfoVO.getSkus();
        if(CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(skuInfoVO -> {
            //2.1 新增skuInfo
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(skuInfoVO,skuInfoEntity);
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString());
            skuInfoEntity.setSpuId(spuId);
            List<String> images = skuInfoVO.getImages();
            //设置默认图片
            if(!CollectionUtils.isEmpty(images)){
                skuInfoEntity.setSkuDefaultImg(StringUtils.isNoneBlank(skuInfoEntity.getSkuDefaultImg()) ? skuInfoEntity.getSkuDefaultImg():images.get(0));
            }

            this.skuInfoDao.insert(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();


            //2.2 新增sku图片
            if(!CollectionUtils.isEmpty(images)){
                images.forEach(image->{
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(StringUtils.equals(image,skuInfoEntity.getSkuDefaultImg()) ? 1 : 0);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setImgUrl(image);
                    this.skuImagesDao.insert(skuImagesEntity);
                });
            }

            //2.3 新增销售属性
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            if(!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(saleAttr->{
                    saleAttr.setSkuId(skuId);
                    saleAttr.setAttrSort(0);
                    this.saleAttrValueDao.insert(saleAttr);

                });
            }


            //新增营销相关的3张表
            SaleVO saleVO = new SaleVO();
            BeanUtils.copyProperties(skuInfoVO,saleVO);
            saleVO.setSkuId(skuId);
            this.smsClient.saveSale(saleVO);


        });
    }

    private void saveBaseAttr(SpuInfoVO spuInfoVO, Long spuId) {
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        baseAttrs.forEach(baseAttr->{
            baseAttr.setSpuId(spuId);
            baseAttr.setAttrSort(0);
            baseAttr.setQuickShow(1);
            this.productAttrValueDao.insert(baseAttr);
        });
    }

    private Long saveSpuInfo(SpuInfoVO spuInfoVO) {
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        this.save(spuInfoVO);
        return spuInfoVO.getId();
    }

}
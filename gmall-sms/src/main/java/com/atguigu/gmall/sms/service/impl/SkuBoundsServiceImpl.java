package com.atguigu.gmall.sms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SaleVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuLadderDao skuLadderDao;
    @Autowired
    private SkuFullReductionDao fullReductionDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public void saveSale(SaleVO saleVO) {

        //3.1 新增积分skuBounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setBuyBounds(saleVO.getBuyBounds());
        skuBoundsEntity.setGrowBounds(saleVO.getGrowBounds());
        skuBoundsEntity.setSkuId(saleVO.skuId);
        List<Integer> works = saleVO.getWork();
        if (!CollectionUtils.isEmpty(works) && works.size() == 4) {
            skuBoundsEntity.setWork(works.get(3) * 1 + works.get(2) * 2 + works.get(1) * 4 + works.get(0) * 8);
        }
        this.save(skuBoundsEntity);

        //3.2 新增打折信息 skuLadder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setFullCount(saleVO.getFullCount());
        skuLadderEntity.setDiscount(saleVO.getDiscount());
        skuLadderEntity.setAddOther(saleVO.getLadderAddOther());
        skuLadderEntity.setSkuId(saleVO.skuId);
        this.skuLadderDao.insert(skuLadderEntity);

        //3.3 新增满减信息 skuReduction
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        fullReductionEntity.setAddOther(saleVO.getFullAddOther());
        fullReductionEntity.setFullPrice(saleVO.getFullPrice());
        fullReductionEntity.setReducePrice(saleVO.getReducePrice());
        fullReductionEntity.setSkuId(saleVO.skuId);
        this.fullReductionDao.insert(fullReductionEntity);

    }

    @Override
    public List<ItemSaleVo> queryItemSaleVOs(long skuId) {

        List<ItemSaleVo> itemSaleVos = new ArrayList<>();
        //查询积分信息
        List<SkuBoundsEntity> skuBoundsEntities = this.list(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (!CollectionUtils.isEmpty(skuBoundsEntities)) {
            ItemSaleVo saleVo = new ItemSaleVo();
            saleVo.setType("积分");
            BigDecimal buyBounds = skuBoundsEntities.get(0).getBuyBounds();
            BigDecimal growBounds = skuBoundsEntities.get(0).getGrowBounds();
            saleVo.setDesc("购物积分赠送" + buyBounds.intValue() + "成长积分赠送" + growBounds.intValue());
            itemSaleVos.add(saleVo);
        }
//      查询满减信息
        List<SkuFullReductionEntity> skuFullReductionEntities = this.fullReductionDao.selectList(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (!CollectionUtils.isEmpty(skuFullReductionEntities)) {
            ItemSaleVo saleVo = new ItemSaleVo();
            saleVo.setType("满减");
            BigDecimal fullPrice = skuFullReductionEntities.get(0).getFullPrice();
            BigDecimal reducePrice = skuFullReductionEntities.get(0).getReducePrice();
            saleVo.setDesc("购物满" + fullPrice.intValue() + "减" + reducePrice.intValue());
            itemSaleVos.add(saleVo);
        }
//        查询打折信息
        List<SkuLadderEntity> skuLadderEntities = this.skuLadderDao.selectList(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (!CollectionUtils.isEmpty(skuLadderEntities)) {
            ItemSaleVo saleVo = new ItemSaleVo();
            saleVo.setType("打折");
            Integer fullCount = skuLadderEntities.get(0).getFullCount();
            BigDecimal discount = skuLadderEntities.get(0).getDiscount();
            saleVo.setDesc("购物满" + fullCount + "件，打" + discount.divide(new BigDecimal(10)).floatValue() + "折");
            itemSaleVos.add(saleVo);
        }
        return itemSaleVos;
    }

}
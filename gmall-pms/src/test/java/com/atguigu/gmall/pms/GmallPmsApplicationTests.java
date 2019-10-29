package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.dao.BrandDao;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GmallPmsApplicationTests {


    @Autowired
    private BrandDao brandDao;

    @Test
    void contextLoads() {
    }

    @Test
    public void test(){
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setDescript("aaaaaa");
//        brandEntity.setFirstLetter("A");
//        brandEntity.setLogo("www.baidu.com/log.gif");
//        brandEntity.setName("aa");
//        brandEntity.setShowStatus(0);
//        brandEntity.setSort(1);
//        this.brandDao.insert(brandEntity);

//        Map<String, Object> map = new HashMap<>();
//        map.put("name","fds发多少");
//        this.brandDao.deleteByMap(map);

//        System.out.println(this.brandDao.selectList(new QueryWrapper<BrandEntity>().eq("name", "aa")));

        IPage<BrandEntity> page = this.brandDao.selectPage(new Page<BrandEntity>(2, 2), new QueryWrapper<BrandEntity>());
        System.out.println(page.getRecords());
        System.out.println(page.getTotal());
        System.out.println(page.getPages());


    }


}

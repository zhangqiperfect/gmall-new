package com.atguigu.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ZQ
 * @create 2019-11-08 16:49
 */
@RestController
@RequestMapping("index")
public class GmallIndexController {
     @Autowired
     private IndexService indexService;
     @GetMapping("cates")
     public Resp<List<CategoryEntity>> queryLevel1Categlories(){
       List<CategoryEntity> categoryEntities=this.indexService.queryLevel1Categlories();
         System.out.println(categoryEntities);
         return Resp.ok(categoryEntities);
     }

     @GetMapping("cates/{pid}")
    public Resp<List<CategoryVo>> queryCategoryVO(@PathVariable("pid")long pid){
         List<CategoryVo> categoryVos= this.indexService.queryCategoryVO(pid);
         return Resp.ok(categoryVos);
     }
     @GetMapping("testLock")
    public Resp<Object> testLock(HttpServletRequest request){
        String msg = this.indexService.testLock();
         System.out.println(request.getLocalPort());
         return  Resp.ok(msg);
     }
}

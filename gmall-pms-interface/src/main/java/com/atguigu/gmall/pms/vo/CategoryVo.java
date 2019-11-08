package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * @author ZQ
 * @create 2019-11-08 19:11
 */
@Data
public class CategoryVo extends CategoryEntity {
    private List<CategoryEntity> subs;
}

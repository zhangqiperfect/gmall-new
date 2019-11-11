package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

/**
 * @author ZQ
 * @create 2019-11-10 13:43
 */
@Data
public class GroupVO {
    private List<ProductAttrValueEntity> baseAttrValues;
    private String groupName;
}

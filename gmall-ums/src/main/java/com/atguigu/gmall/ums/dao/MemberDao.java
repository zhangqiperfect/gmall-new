package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.api.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author qinhan
 * @email 1589125792@qq.com
 * @date 2019-10-28 20:53:31
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}

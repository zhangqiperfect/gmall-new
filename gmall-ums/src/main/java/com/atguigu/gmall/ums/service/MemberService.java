package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.ums.api.entity.MemberEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 会员
 *
 * @author qinhan
 * @email 1589125792@qq.com
 * @date 2019-10-28 20:53:31
 */
public interface MemberService extends IService<MemberEntity> {

    PageVo queryPage(QueryCondition params);

    boolean checkData(String data, Integer type);

    void register(MemberEntity memberEntity,String code);

    MemberEntity queryUser(String username, String password);
}


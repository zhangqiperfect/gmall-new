package com.atguigu.gmall.ums.api.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.ums.api.dao.MemberDao;
import com.atguigu.gmall.ums.api.entity.MemberEntity;
import com.atguigu.gmall.ums.api.service.MemberService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public boolean checkData(String data, Integer type) {
        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                queryWrapper.eq("username", data);
                break;
            case 2:
                queryWrapper.eq("mobile", data);
                break;
            case 3:
                queryWrapper.eq("email", data);
                break;
            default:
                return false;
        }
        int count = this.count(queryWrapper);
        return count == 0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) {
        String phoneNo = memberEntity.getMobile();
//        1、校验手机号、校验验证码
        String codeStr = this.redisTemplate.opsForValue().get(phoneNo);
        if (StringUtils.isEmpty(codeStr) || !codeStr.equals(code)) {
            throw new IllegalArgumentException("验证码错误");
        }
//       加盐加密
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        memberEntity.setSalt(salt);
//        注册功能
        memberEntity.setPassword(DigestUtils.md5Hex(memberEntity.getPassword() + salt));
        memberEntity.setCreateTime(new Date());
        memberEntity.setLevelId(1l);
        memberEntity.setGrowth(0);
        memberEntity.setIntegration(0);
        memberEntity.setStatus(1);
//        删除redis中的验证码
        this.save(memberEntity);
    }

    @Override
    public MemberEntity queryUser(String username, String password) {
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", username));
        //根据用户名进行查询，不成功则为用户名不合法
        if (memberEntity == null) {
            throw new IllegalArgumentException("用户名不合法");
        }
        String s = DigestUtils.md5Hex(password + memberEntity.getSalt());
        if (!StringUtils.equals(s, memberEntity.getPassword())) {
            throw new IllegalArgumentException("密码不合法");
        }
        return memberEntity;
    }

}
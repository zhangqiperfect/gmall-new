package com.atguigu.gmall.ums.api.controller;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.msm.util.RegexUtil;
import com.atguigu.gmall.ums.api.entity.MemberEntity;
import com.atguigu.gmall.ums.api.feign.GmallMsmClient;
import com.atguigu.gmall.ums.api.service.MemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;


/**
 * 会员
 *
 * @author qinhan
 * @email 1589125792@qq.com
 * @date 2019-10-28 20:53:31
 */
@Api(tags = "会员 管理")
@RestController
@RequestMapping("ums/member")
public class MemberController {
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private GmallMsmClient gmallMsmClient;
    @Autowired
    private MemberService memberService;

    @PostMapping("code")
    public Resp<Object> getCode(@RequestParam("phoneNo") String phoneNo) {
        boolean b = RegexUtil.ckeckPhone(phoneNo);
        if (!b) {
            return Resp.fail("手机号不合法");
    }
        amqpTemplate.convertAndSend("GMALL-CODE-EXCHANGE", "code.", phoneNo);
        //  this.gmallMsmClient.sendCode(phoneNo);
        return Resp.ok("验证码发送成功");
    }

    @PostMapping("register")
    public Resp<Object> register(MemberEntity memberEntity, @RequestParam("code") String code) {
        this.memberService.register(memberEntity, code);
        return null;
    }

    @GetMapping("check/{param}/{type}")
    public Resp<Boolean> checkData(@PathVariable("param") String data, @PathVariable("type") Integer type) {
        boolean b = this.memberService.checkData(data, type);
        return Resp.ok(b);
    }

    @GetMapping("query")
    public Resp<MemberEntity> queryUser(@RequestParam("username") String username, @RequestParam("password") String password) {
        MemberEntity memberEntity = this.memberService.queryUser(username, password);
        return Resp.ok(memberEntity);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ums:member:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = memberService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('ums:member:info')")
    public Resp<MemberEntity> info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return Resp.ok(member);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ums:member:save')")
    public Resp<Object> save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ums:member:update')")
    public Resp<Object> update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ums:member:delete')")
    public Resp<Object> delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return Resp.ok(null);
    }

}

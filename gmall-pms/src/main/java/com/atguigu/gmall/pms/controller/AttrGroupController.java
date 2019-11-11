package com.atguigu.gmall.pms.controller;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.atguigu.gmall.pms.vo.GroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


/**
 * 属性分组
 *
 * @author qinhan
 * @email 1589125792@qq.com
 * @date 2019-10-28 20:41:49
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private ProductAttrValueDao attrValueDao;
    @GetMapping("item/group/{cid}/{spuId}")
    public Resp<List<GroupVO>> queryGroupVOByCid(@PathVariable("cid") long cid,@PathVariable("spuId")long spuId) {
        List<GroupVO> groupVOS = this.attrGroupService.queryGroupVOByCid(cid,spuId);
        return Resp.ok(groupVOS);
    }

    @GetMapping("withattrs/cat/{catId}")
    public Resp<List<AttrGroupVO>> queryGroupWithAttrByCid(@PathVariable("catId") Long catId) {
        List<AttrGroupVO> attrGroupVOS = this.attrGroupService.queryGroupWithAttrByCid(catId);
        return Resp.ok(attrGroupVOS);
    }

    @ApiOperation("根据分组id查询分组及组下的规格参数")
    @GetMapping("withattr/{gid}")
    public Resp<AttrGroupVO> queryGroupWithAttrs(@PathVariable("gid") Long gid) {
        AttrGroupVO attrGroupVO = this.attrGroupService.queryGroupWithAttrs(gid);
        return Resp.ok(attrGroupVO);

    }

    @ApiOperation("根据三级分类id分页查询")
    @GetMapping("{catId}")
    public Resp<PageVo> queryByCidPage(@PathVariable("catId") Long catId, QueryCondition condition) {
        PageVo pageVo = this.attrGroupService.queryByCidPage(catId, condition);
        return Resp.ok(pageVo);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attrgroup:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrGroupService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrGroupId}")
    @PreAuthorize("hasAuthority('pms:attrgroup:info')")
    public Resp<AttrGroupEntity> info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return Resp.ok(attrGroup);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attrgroup:save')")
    public Resp<Object> save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attrgroup:update')")
    public Resp<Object> update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attrgroup:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return Resp.ok(null);
    }

}

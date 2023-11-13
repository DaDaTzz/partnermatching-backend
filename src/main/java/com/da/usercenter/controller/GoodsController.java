package com.da.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.ThrowUtils;
import com.da.usercenter.model.dto.goods.GoodsQueryRequest;
import com.da.usercenter.model.dto.post.PostQueryRequest;
import com.da.usercenter.model.entity.Goods;
import com.da.usercenter.model.entity.Post;
import com.da.usercenter.model.vo.PostVO;
import com.da.usercenter.service.GoodsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 商品接口
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Resource
    private GoodsService goodsService;

    /**
     * 分页获取列表（封装类）
     *
     * @param goodsQueryRequest
     * @return
     */
    @GetMapping("/list/page")
    public ResponseResult<Page<Goods>> listPostVOByPage(GoodsQueryRequest goodsQueryRequest) {
        long current = goodsQueryRequest.getCurrent();
        long size = goodsQueryRequest.getPageSize();
        QueryWrapper<Goods> queryWrapper = goodsService.getQueryWrapper(goodsQueryRequest);
        Page<Goods> goodsPage = goodsService.page(new Page<>(current, size), queryWrapper);
        return ResponseResult.success(goodsPage);
    }
}

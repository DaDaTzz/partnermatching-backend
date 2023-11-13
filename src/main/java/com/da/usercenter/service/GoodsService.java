package com.da.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.da.usercenter.model.dto.goods.GoodsQueryRequest;
import com.da.usercenter.model.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.da.usercenter.model.entity.Post;

/**
* @author 达
* @description 针对表【goods(商品)】的数据库操作Service
* @createDate 2023-11-13 20:32:37
*/
public interface GoodsService extends IService<Goods> {

    QueryWrapper<Goods> getQueryWrapper(GoodsQueryRequest goodsQueryRequest);
}

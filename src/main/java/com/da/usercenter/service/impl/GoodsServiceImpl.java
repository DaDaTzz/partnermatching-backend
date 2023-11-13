package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.constant.CommonConstant;
import com.da.usercenter.model.dto.goods.GoodsQueryRequest;
import com.da.usercenter.model.entity.Goods;
import com.da.usercenter.model.entity.Post;
import com.da.usercenter.service.GoodsService;
import com.da.usercenter.mapper.GoodsMapper;
import com.da.usercenter.utils.SqlUtils;
import org.springframework.stereotype.Service;

/**
* @author 达
* @description 针对表【goods(商品)】的数据库操作Service实现
* @createDate 2023-11-13 20:32:37
*/
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods>
    implements GoodsService{

    @Override
    public QueryWrapper<Goods> getQueryWrapper(GoodsQueryRequest goodsQueryRequest) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        if (goodsQueryRequest == null) {
            return queryWrapper;
        }
        Long id = goodsQueryRequest.getId();
        String name = goodsQueryRequest.getName();
        Long price = goodsQueryRequest.getPrice();
        String description = goodsQueryRequest.getDescription();
        String sortField = goodsQueryRequest.getSortField();
        String sortOrder = goodsQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq("is_delete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}





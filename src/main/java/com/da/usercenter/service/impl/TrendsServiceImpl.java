package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.constant.CommonConstant;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.exception.ThrowUtils;
import com.da.usercenter.model.dto.trends.TrendsQueryRequest;
import com.da.usercenter.model.entity.Post;
import com.da.usercenter.model.entity.Trends;
import com.da.usercenter.service.TrendsService;
import com.da.usercenter.mapper.TrendsMapper;
import com.da.usercenter.utils.SqlUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 达
* @description 针对表【trends(朋友圈)】的数据库操作Service实现
* @createDate 2023-12-25 12:09:29
*/
@Service
public class TrendsServiceImpl extends ServiceImpl<TrendsMapper, Trends>
    implements TrendsService{

    @Override
    public QueryWrapper<Trends> getQueryWrapper(TrendsQueryRequest trendsQueryRequest) {
        QueryWrapper<Trends> queryWrapper = new QueryWrapper<>();
        if (trendsQueryRequest == null) {
            return queryWrapper;
        }

        Long id = trendsQueryRequest.getId();
        String content = trendsQueryRequest.getContent();
        Long userId = trendsQueryRequest.getUserId();
        String sortField = trendsQueryRequest.getSortField();
        String sortOrder = trendsQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq("is_delete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public void validTrends(Trends trends, boolean add) {
        if (trends == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String content = trends.getContent();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(content), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(content) && content.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容");
        }
    }
}





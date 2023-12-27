package com.da.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.da.usercenter.model.dto.post.PostQueryRequest;
import com.da.usercenter.model.dto.trends.TrendsQueryRequest;
import com.da.usercenter.model.entity.Post;
import com.da.usercenter.model.entity.Trends;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 达
* @description 针对表【trends(朋友圈)】的数据库操作Service
* @createDate 2023-12-25 12:09:29
*/
public interface TrendsService extends IService<Trends> {

    QueryWrapper<Trends> getQueryWrapper(TrendsQueryRequest trendsQueryRequest);

    void validTrends(Trends trends, boolean add);
}

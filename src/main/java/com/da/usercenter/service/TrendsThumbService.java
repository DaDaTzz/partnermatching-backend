package com.da.usercenter.service;

import com.da.usercenter.model.entity.TrendsThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.da.usercenter.model.entity.User;

/**
* @author 达
* @description 针对表【trends_thumb(动态点赞)】的数据库操作Service
* @createDate 2023-12-28 13:10:31
*/
public interface TrendsThumbService extends IService<TrendsThumb> {

    int doTrendsThumb(long trendsId, User loginUser);

    int doTrendsThumbInner(long userId, long trendsId);
}

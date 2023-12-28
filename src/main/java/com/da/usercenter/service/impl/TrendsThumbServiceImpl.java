package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.*;
import com.da.usercenter.service.PostThumbService;
import com.da.usercenter.service.TrendsService;
import com.da.usercenter.service.TrendsThumbService;
import com.da.usercenter.mapper.TrendsThumbMapper;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
* @author 达
* @description 针对表【trends_thumb(动态点赞)】的数据库操作Service实现
* @createDate 2023-12-28 13:10:31
*/
@Service
public class TrendsThumbServiceImpl extends ServiceImpl<TrendsThumbMapper, TrendsThumb>
    implements TrendsThumbService{

    @Resource
    private TrendsService trendsService;

    /**
     * 点赞
     * @param trendsId
     * @param loginUser
     * @return
     */
    @Override
    public int doTrendsThumb(long trendsId, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        Trends trends = trendsService.getById(trendsId);
        if (trends == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 是否已点赞
        long userId = loginUser.getId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        TrendsThumbService trendsThumbService = (TrendsThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return trendsThumbService.doTrendsThumbInner(userId, trendsId);
        }
    }

    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param trendsId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doTrendsThumbInner(long userId, long trendsId) {
        TrendsThumb trendsThumb = new TrendsThumb();
        trendsThumb.setUserId(userId);
        trendsThumb.setTrendsId(trendsId);
        QueryWrapper<TrendsThumb> thumbQueryWrapper = new QueryWrapper<>(trendsThumb);
        TrendsThumb oldTrendsThumb = this.getOne(thumbQueryWrapper);
        boolean result;
        // 已点赞
        if (oldTrendsThumb != null) {
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 点赞数 - 1
                result = trendsService.update()
                        .eq("id", trendsId)
                        .gt("thumb_num", 0)
                        .setSql("thumb_num = thumb_num - 1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未点赞
            result = this.save(trendsThumb);
            if (result) {
                // 点赞数 + 1
                result = trendsService.update()
                        .eq("id", trendsId)
                        .setSql("thumb_num = thumb_num + 1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}





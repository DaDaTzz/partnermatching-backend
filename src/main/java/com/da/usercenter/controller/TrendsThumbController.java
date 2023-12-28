package com.da.usercenter.controller;

import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.dto.postthumb.PostThumbAddRequest;
import com.da.usercenter.model.dto.trends.TrendsThumbAddRequest;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.TrendsThumbService;
import com.da.usercenter.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 动态点赞接口
 *
 * @author 达
 */
@RestController
@RequestMapping("/trends_thumb")
public class TrendsThumbController {

    @Resource
    private TrendsThumbService trendsThumbService;

    @Resource
    private UserService userService;


    /**
     * 点赞 / 取消点赞
     *
     * @param trendsThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public ResponseResult<Integer> doThumb(@RequestBody TrendsThumbAddRequest trendsThumbAddRequest,
                                           HttpServletRequest request) {
        if (trendsThumbAddRequest == null || trendsThumbAddRequest.getTrendsId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getCurrentUser(request);
        long trendsId = trendsThumbAddRequest.getTrendsId();
        int result = trendsThumbService.doTrendsThumb(trendsId, loginUser);
        return ResponseResult.success(result);
    }

}

package com.da.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.Trends;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.entity.UserFollows;
import com.da.usercenter.model.vo.UserTrendsVO;
import com.da.usercenter.service.TrendsService;
import com.da.usercenter.service.UserFollowsService;
import com.da.usercenter.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/trends")
public class TrendsController {

    @Resource
    private TrendsService trendsService;
    @Resource
    private UserService userService;
    @Resource
    private UserFollowsService userFollowsService;


    /**
     * 查询自己以及关注的用户的朋友圈
     *
     * @param request
     * @return
     */
    @GetMapping("/")
    public ResponseResult<List<UserTrendsVO>> listUserTrendsVOByPage(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long currentUserId = currentUser.getId();
        // 自己的动态
        LambdaQueryWrapper<Trends> trendsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        trendsLambdaQueryWrapper.eq(Trends::getUserId, currentUserId);
        List<Trends> currentTrends = trendsService.list(trendsLambdaQueryWrapper);
        // 关注的用户动态
        LambdaQueryWrapper<UserFollows> userFollowsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFollowsLambdaQueryWrapper.eq(UserFollows::getUserId, currentUserId).eq(UserFollows::getIsFollow, 1);
        List<UserFollows> userFollows = userFollowsService.list(userFollowsLambdaQueryWrapper);
        List<Trends> followUserTrends = new ArrayList<>();
        for (UserFollows userFollow : userFollows) {
            Long loveId = userFollow.getLoveId();
            LambdaQueryWrapper<Trends> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Trends::getUserId, loveId);
            List<Trends> followTrends = trendsService.list(queryWrapper);
            for (Trends followTrend : followTrends) {
                followUserTrends.add(followTrend);
            }
        }
        //currentTrends.forEach(trends -> System.out.println(trends));
        //followUserTrends.forEach(trends -> System.out.println(trends));
        // 合并两个 list
        ArrayList<Trends> allTrends = new ArrayList<>();
        allTrends.addAll(currentTrends);
        allTrends.addAll(followUserTrends);
        // 填充数据
        ArrayList<UserTrendsVO> userTrendsVOS = new ArrayList<>();
        for (Trends allTrend : allTrends) {
            Long userId = allTrend.getUserId();
            User user = userService.getById(userId);
            UserTrendsVO userTrendsVO = new UserTrendsVO();
            BeanUtils.copyProperties(user, userTrendsVO);
            BeanUtils.copyProperties(allTrend, userTrendsVO);
            userTrendsVOS.add(userTrendsVO);
        }
        // 根据创建时间（发动态时间）降序排序
        Collections.sort(userTrendsVOS, new Comparator<UserTrendsVO>() {
            @Override
            public int compare(UserTrendsVO o1, UserTrendsVO o2) {
                return o2.getCreateTime().compareTo(o1.getCreateTime());
            }
        });
        return ResponseResult.success(userTrendsVOS);
    }


}

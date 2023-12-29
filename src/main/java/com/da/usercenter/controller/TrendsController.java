package com.da.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.exception.ThrowUtils;
import com.da.usercenter.model.dto.post.PostEditRequest;
import com.da.usercenter.model.dto.trends.TrendsEditRequest;
import com.da.usercenter.model.dto.trends.TrendsQueryRequest;
import com.da.usercenter.model.entity.Post;
import com.da.usercenter.model.entity.Trends;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.entity.UserFollows;
import com.da.usercenter.model.vo.UserTrendsVO;
import com.da.usercenter.service.TrendsService;
import com.da.usercenter.service.UploadService;
import com.da.usercenter.service.UserFollowsService;
import com.da.usercenter.service.UserService;
import com.google.gson.Gson;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/trends")
public class TrendsController {

    @Resource
    private TrendsService trendsService;
    @Resource
    private UserService userService;
    @Resource
    private UserFollowsService userFollowsService;
    @Resource
    private UploadService uploadService;

    private final static Gson GSON = new Gson();


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

    /**
     * 分页获取用户动态信息
     *
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public ResponseResult<Page<Trends>> getTrendsByPage(HttpServletRequest request, TrendsQueryRequest trendsQueryRequest) {
        long current = trendsQueryRequest.getCurrent();
        long size = trendsQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 60, ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 只有管理员有权限查询所有用户动态信息
        if (currentUser.getType() != 1) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<Trends> queryWrapper = trendsService.getQueryWrapper(trendsQueryRequest);
        Page<Trends> trendsPage = trendsService.page(new Page<>(current, size), queryWrapper);
        return ResponseResult.success(trendsPage);

    }


    /**
     * 删除动态（可根据动态的 id，用户 id删除）
     *
     * @param request
     * @param trends
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> deleteTrendsById(HttpServletRequest request, @RequestBody Trends trends) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long currentUserId = currentUser.getId();
        Long userId = trends.getUserId();
        // ============== 根据 用户 id 删除==================
        if(userId != null && userId > 0){
            LambdaQueryWrapper<Trends> trendsLambdaQueryWrapper = new LambdaQueryWrapper<>();
            trendsLambdaQueryWrapper.eq(Trends::getUserId, userId);
            List<Trends> trendsList = trendsService.list(trendsLambdaQueryWrapper);
            if(trendsList == null || trendsList.size() == 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            boolean res = trendsService.remove(trendsLambdaQueryWrapper);
            if(!res){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            return ResponseResult.success(true);
        }
        // ============== 根据 动态 id 删除==================
        Trends t = trendsService.getById(trends.getId());
        if (t == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long trendsUserId = t.getUserId();
        // 仅发动态本人或管理员可以删除动态
        if (currentUserId != trendsUserId && currentUser.getType() != 1) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = trendsService.removeById(trends.getId());
        return ResponseResult.success(result);
    }


    /**
     * 新增动态（发朋友圈）
     *
     * @param files   图片文件流
     * @param content 内容
     * @param request 请求对象
     * @return 新动态 ID
     */
    @PostMapping("/add")
    public ResponseResult<Long> addTrends(@RequestParam("file") MultipartFile[] files, @RequestParam("content") String content, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Trends trends = new Trends();
        Trends t = new Trends();
        t.setContent(content);
        BeanUtils.copyProperties(t, trends);
        trendsService.validTrends(trends, true);
        User loginUser = userService.getCurrentUser(request);
        trends.setUserId(loginUser.getId());
        trends.setThumbNum(0);
        boolean result = trendsService.save(trends);
        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR);
        long newTrendsId = trends.getId();

        // 存储上传的朋友圈图片（Gitee 仓库）
        try {
            List<String> imgUrlList = uploadService.uploadImg(files, newTrendsId);
            String imgsJson = GSON.toJson(imgUrlList);
            Trends newTrends = new Trends();
            newTrends.setId(newTrendsId);
            newTrends.setImg(imgsJson);
            trendsService.updateById(newTrends);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseResult.success(newTrendsId);

    }



    /**
     * 编辑（更新）
     *
     * @param trendsEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public ResponseResult<Boolean> editTrends(@RequestBody TrendsEditRequest trendsEditRequest, HttpServletRequest request) {
        if (trendsEditRequest == null || trendsEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Trends trends = new Trends();
        BeanUtils.copyProperties(trendsEditRequest, trends);
        // 参数校验
        trendsService.validTrends(trends, false);
        User loginUser = userService.getCurrentUser(request);
        long id = trendsEditRequest.getId();
        // 判断是否存在
        Trends oldTrends = trendsService.getById(id);
        ThrowUtils.throwIf(oldTrends == null, ErrorCode.PARAMS_ERROR);
        // 仅本人或管理员可编辑
        if (!oldTrends.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = trendsService.updateById(trends);
        return ResponseResult.success(result);
    }







}

package com.da.usercenter.controller;


import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.dto.postthumb.PostThumbAddRequest;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.PostThumbService;
import com.da.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子点赞接口
 *
 * @author Da
 */
@RestController
@RequestMapping("/post_thumb")
@Slf4j
@CrossOrigin(origins = {"http://8.130.133.165"},allowCredentials = "true")
//@CrossOrigin(origins = {"http://127.0.0.1:5173"},allowCredentials = "true")
public class PostThumbController {

    @Resource
    private PostThumbService postThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param postThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public ResponseResult<Integer> doThumb(@RequestBody PostThumbAddRequest postThumbAddRequest,
                                           HttpServletRequest request) {
        if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getCurrentUser(request);
        long postId = postThumbAddRequest.getPostId();
        int result = postThumbService.doPostThumb(postId, loginUser);
        return ResponseResult.success(result);
    }

}

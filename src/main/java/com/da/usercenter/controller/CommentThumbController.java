package com.da.usercenter.controller;


import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.dto.commentthumb.CommentThumbAddRequest;
import com.da.usercenter.model.dto.postthumb.PostThumbAddRequest;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.CommentThumbService;
import com.da.usercenter.service.PostThumbService;
import com.da.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 评论点赞接口
 *
 * @author Da
 */
@RestController
@RequestMapping("/comment_thumb")
@Slf4j
//@CrossOrigin(origins = {"http://8.130.133.165"},allowCredentials = "true")
//@CrossOrigin(origins = {"http://127.0.0.1:5173"},allowCredentials = "true")
public class CommentThumbController {

    @Resource
    private CommentThumbService commentThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param commentThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public ResponseResult<Integer> doThumb(@RequestBody CommentThumbAddRequest commentThumbAddRequest,
                                           HttpServletRequest request) {
        if (commentThumbAddRequest == null || commentThumbAddRequest.getCommentId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getCurrentUser(request);
        long commentId = commentThumbAddRequest.getCommentId();
        int result = commentThumbService.doCommentThumb(commentId, loginUser);
        return ResponseResult.success(result);
    }

}

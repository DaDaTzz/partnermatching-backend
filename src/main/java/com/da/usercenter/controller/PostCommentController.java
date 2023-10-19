package com.da.usercenter.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.dto.post.DelCommentRequest;
import com.da.usercenter.model.dto.postcomment.AddCommentRequest;
import com.da.usercenter.model.entity.PostComment;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.PostCommentService;
import com.da.usercenter.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.da.usercenter.constant.UserConstant.ADMIN_USER;

/**
 * 帖子评论接口
 */
@RestController
@RequestMapping("/post_comment")
//@CrossOrigin(origins = {"http://8.130.133.165"},allowCredentials = "true")
//@CrossOrigin(origins = {"http://127.0.0.1:5173"},allowCredentials = "true")
public class PostCommentController {

    @Resource
    private PostCommentService postCommentService;
    @Resource
    private UserService userService;

    @PostMapping("/add")
    public ResponseResult<Long> addComment(@RequestBody AddCommentRequest addCommentRequest, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long postId = addCommentRequest.getPostId();
        String content = addCommentRequest.getContent();
        if(postId == null || StringUtils.isBlank(content)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long userid = currentUser.getId();
        PostComment postComment = new PostComment();
        postComment.setPostId(postId);
        postComment.setUserId(userid);
        postComment.setContent(content);
        postCommentService.save(postComment);
        return ResponseResult.success(postComment.getId());
    }

    @PostMapping("/del")
    public ResponseResult<Boolean> delComment(@RequestBody DelCommentRequest delCommentRequest, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if(delCommentRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PostComment postComment = postCommentService.getById(delCommentRequest.getId());
        // 帖子的创建人、自己写的评论、管理员 才有权限删除
        if(postComment.getUserId() != currentUser.getId()){
            if(ADMIN_USER.equals(currentUser.getType()) || delCommentRequest.getCreateUserId() == currentUser.getId()){
                return ResponseResult.success(postCommentService.removeById(delCommentRequest.getId()));
            }
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return ResponseResult.success(postCommentService.removeById(delCommentRequest.getId()));

    }

}

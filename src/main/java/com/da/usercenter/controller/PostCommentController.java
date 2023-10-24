package com.da.usercenter.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.mapper.PostCommentMapper;
import com.da.usercenter.mapper.PostMapper;
import com.da.usercenter.model.dto.post.DelCommentRequest;
import com.da.usercenter.model.dto.postcomment.AddCommentRequest;
import com.da.usercenter.model.entity.PostComment;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.vo.LikeMyVO;
import com.da.usercenter.service.PostCommentService;
import com.da.usercenter.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.*;

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
    @Resource
    private PostCommentMapper postCommentMapper;
    @Resource
    private PostMapper postMapper;

    /**
     * 添加评论
     * @param addCommentRequest
     * @param request
     * @return
     */
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

    /**
     * 删除评论
     * @param delCommentRequest
     * @param request
     * @return
     */
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

    /**
     * 获取给我点赞的用户信息以及内容
     * @return
     */
    @GetMapping("/like/my")
    public ResponseResult<List<LikeMyVO>> getLikeMyCommentList(HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long currentUserId = currentUser.getId();
        // 获取给我评论点赞的用户信息以及内容
        List<Map<String, Object>> likeMyCommentUserIdList = postCommentMapper.getLikeMyCommentUserIdList(currentUserId);
        ArrayList<LikeMyVO> likeMyVOS = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : likeMyCommentUserIdList) {
            LikeMyVO likeMyVO = new LikeMyVO();
            likeMyVO.setUserId((Long) stringObjectMap.get("点赞的用户id"));
            String userId = stringObjectMap.get("点赞的用户id").toString();
            User user = userService.getById(userId);
            likeMyVO.setPostId((Long) stringObjectMap.get("文章id"));
            likeMyVO.setNickname(user.getNickname());
            likeMyVO.setAvatar(user.getProfilePhoto());
            likeMyVO.setContent((String) stringObjectMap.get("内容"));
            likeMyVO.setThumbTime((LocalDateTime) stringObjectMap.get("点赞时间"));
            likeMyVO.setCommentOrPost(0);
            likeMyVOS.add(likeMyVO);
        }
        // 获取给我博客点赞的用户信息以及内容
        List<Map<String, Object>> likeMyPostUserIdList = postMapper.getLikeMyPostUserIdList(currentUserId);
        for (Map<String, Object> stringObjectMap : likeMyPostUserIdList) {
            LikeMyVO likeMyVO = new LikeMyVO();
            likeMyVO.setTitle(stringObjectMap.get("标题").toString());
            likeMyVO.setImg(stringObjectMap.get("img").toString());
            likeMyVO.setContent(stringObjectMap.get("内容").toString());
            likeMyVO.setPostId((Long) stringObjectMap.get("文章id"));
            String userId = stringObjectMap.get("点赞的用户id").toString();
            User user = userService.getById(userId);
            likeMyVO.setUserId(user.getId());
            likeMyVO.setNickname(user.getNickname());
            likeMyVO.setAvatar(user.getProfilePhoto());
            likeMyVO.setCommentOrPost(1);
            likeMyVO.setThumbTime((LocalDateTime) stringObjectMap.get("点赞时间"));
            likeMyVOS.add(likeMyVO);
        }
        // 根据点赞时间倒序
        Collections.sort(likeMyVOS, Comparator.comparing(LikeMyVO::getThumbTime).reversed());
        return ResponseResult.success(likeMyVOS);
    };


}

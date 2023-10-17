package com.da.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.common.DeleteRequest;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.exception.ThrowUtils;
import com.da.usercenter.model.dto.post.PostEditRequest;
import com.da.usercenter.model.dto.post.PostQueryRequest;
import com.da.usercenter.model.dto.post.PostUpdateRequest;
import com.da.usercenter.model.entity.Post;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.vo.PostVO;
import com.da.usercenter.service.PostService;
import com.da.usercenter.service.UploadService;
import com.da.usercenter.service.UserService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * 帖子接口
 *
 * @author Da
 */
@RestController
@RequestMapping("/post")
@Slf4j
//@CrossOrigin(origins = {"http://8.130.133.165"},allowCredentials = "true")
//@CrossOrigin(origins = {"http://127.0.0.1:5173"},allowCredentials = "true")
public class PostController {

    @Resource
    private PostService postService;

    @Resource
    private UploadService uploadService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param
     * @param request
     * @return
     */
    @PostMapping("/add")
    public ResponseResult<Long> addPost(@RequestParam("file") MultipartFile[] files, @RequestParam("title") String title,@RequestParam("content") String content, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if(StringUtils.isAnyBlank(title,content)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = new Post();
        Post p = new Post();
        p.setContent(content);
        p.setTitle(title);
        BeanUtils.copyProperties(p, post);
//        List<String> tags = postAddRequest.getTags();
//        if (tags != null) {
//            post.setTags(GSON.toJson(tags));
//        }
        postService.validPost(post, true);
        User loginUser = userService.getCurrentUser(request);
        post.setUserId(loginUser.getId());
        post.setFavourNum(0);
        post.setThumbNum(0);
        boolean result = postService.save(post);
        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR);
        long newPostId = post.getId();

        // 存博文图片
        try {
            List<String> msgUrlList = uploadService.uploadMsg(files, newPostId);
            String msgsJson = GSON.toJson(msgUrlList);
            Post newPort = new Post();
            newPort.setId(newPostId);
            newPort.setImg(msgsJson);
            postService.updateById(newPort);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseResult.success(newPostId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getCurrentUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.PARAMS_ERROR);
        // 仅本人或管理员可删除
        if (!oldPost.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean b = postService.removeById(id);
        return ResponseResult.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param postUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> updatePost(@RequestBody PostUpdateRequest postUpdateRequest) {
        if (postUpdateRequest == null || postUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateRequest, post);
        List<String> tags = postUpdateRequest.getTags();
        if (tags != null) {
            post.setTags(GSON.toJson(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        long id = postUpdateRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NO_AUTH);
        boolean result = postService.updateById(post);
        return ResponseResult.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public ResponseResult<PostVO> getPostVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = postService.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResponseResult.success(postService.getPostVO(post, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param postQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page/vo")
    public ResponseResult<Page<PostVO>> listPostVOByPage(PostQueryRequest postQueryRequest,
            HttpServletRequest request) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 60, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Post> queryWrapper = postService.getQueryWrapper(postQueryRequest);
        queryWrapper.orderByDesc("thumb_num");
        Page<Post> postPage = postService.page(new Page<>(current, size),
               queryWrapper);
        return ResponseResult.success(postService.getPostVOPage(postPage, request));
    }



    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param postQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public ResponseResult<Page<PostVO>> listMyPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request) {
        if (postQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        postQueryRequest.setUserId(loginUser.getId());
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResponseResult.success(postService.getPostVOPage(postPage, request));
    }

    // endregion



    /**
     * 编辑（用户）
     *
     * @param postEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public ResponseResult<Boolean> editPost(@RequestBody PostEditRequest postEditRequest, HttpServletRequest request) {
        if (postEditRequest == null || postEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postEditRequest, post);
        List<String> tags = postEditRequest.getTags();
        if (tags != null) {
            post.setTags(GSON.toJson(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        User loginUser = userService.getCurrentUser(request);
        long id = postEditRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.PARAMS_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPost.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = postService.updateById(post);
        return ResponseResult.success(result);
    }

}

package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.*;
import com.da.usercenter.service.CommentThumbService;
import com.da.usercenter.mapper.CommentThumbMapper;
import com.da.usercenter.service.PostCommentService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
* @author 达
* @description 针对表【comment_thumb(评论点赞)】的数据库操作Service实现
* @createDate 2023-10-23 13:25:20
*/
@Service
public class CommentThumbServiceImpl extends ServiceImpl<CommentThumbMapper, CommentThumb>
    implements CommentThumbService{

    @Resource
    private PostCommentService postCommentService;


    @Override
    public int doCommentThumb(long commentId, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        PostComment postComment = postCommentService.getById(commentId);
        if (postComment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 是否已点赞
        long userId = loginUser.getId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        CommentThumbService commentThumbService = (CommentThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return commentThumbService.doCommentThumbInner(userId, commentId);
        }
    }

    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param commentId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doCommentThumbInner(long userId, long commentId) {
        CommentThumb commentThumb = new CommentThumb();
        commentThumb.setUserId(userId);
        commentThumb.setCommentId(commentId);
        QueryWrapper<CommentThumb> thumbQueryWrapper = new QueryWrapper<>(commentThumb);
        CommentThumb oldcommentThumb = this.getOne(thumbQueryWrapper);
        boolean result;
        // 已点赞
        if (oldcommentThumb != null) {
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 点赞数 - 1
                result = postCommentService.update()
                        .eq("id", commentId)
                        .gt("thumb_num", 0)
                        .setSql("thumb_num = thumb_num - 1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未点赞
            result = this.save(commentThumb);
            if (result) {
                // 点赞数 + 1
                result = postCommentService.update()
                        .eq("id", commentId)
                        .setSql("thumb_num = thumb_num + 1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}





package com.da.usercenter.service;

import com.da.usercenter.model.entity.CommentThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.da.usercenter.model.entity.User;

/**
* @author 达
* @description 针对表【comment_thumb(评论点赞)】的数据库操作Service
* @createDate 2023-10-23 13:25:20
*/
public interface CommentThumbService extends IService<CommentThumb> {

    int doCommentThumb(long commentId, User loginUser);
    int doCommentThumbInner(long userId, long commentId);

}

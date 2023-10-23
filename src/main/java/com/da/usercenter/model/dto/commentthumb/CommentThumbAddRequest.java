package com.da.usercenter.model.dto.commentthumb;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论点赞请求
 *
 * @author Da
 */
@Data
public class CommentThumbAddRequest implements Serializable {

    /**
     * 帖子 id
     */
    private Long commentId;

    private static final long serialVersionUID = 1L;
}
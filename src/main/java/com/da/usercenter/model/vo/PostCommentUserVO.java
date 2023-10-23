package com.da.usercenter.model.vo;


import lombok.Data;

import java.util.Date;

@Data
public class PostCommentUserVO {
    /**
     * id
     */
    private Long id;

    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 评论用户 id
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论的用户信息
     */
    private UserVO commentUser;

    /**
     * 是否点赞
     */
    private Boolean hasThumb;

    /**
     * 点赞数
     */
    private Long thumbNum;

    /**
     * 是否有权限删除
     */
    private Boolean isCanDelete;

    /**
     * 创建时间
     */
    private Date createTime;
}

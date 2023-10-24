package com.da.usercenter.model.vo;


import io.swagger.models.auth.In;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 给我评论点赞的用户VO
 */
@Data
public class LikeMyVO {
    /**
     * 点赞的用户 id
     */
    private Long userId;

    /**
     * 文章id
     */
    private Long postId;

    /**
     * 点赞的用户昵称
     */
    private String nickname;

    /**
     * 点赞的用户头像
     */
    private String avatar;

    /**
     * 我评论的内容 / 文章内容
     */
    private String content;

    /**
     * 评论或者博客 0-评论 1-博客
     */
    private Integer commentOrPost;


    /**
     * 点赞的文章标题
     */
    private String title;

    /**
     * 点赞的文章图片
     */
    private String img;


    /**
     * 点赞的时间
     */
    private LocalDateTime thumbTime;


}

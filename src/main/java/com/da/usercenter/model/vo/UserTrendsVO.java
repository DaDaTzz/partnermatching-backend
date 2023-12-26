package com.da.usercenter.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户朋友圈 VO 封装类
 */
@Data
public class UserTrendsVO {


    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String profilePhoto;

    /**
     * 朋友圈 id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 图片
     */
    private String img;


}

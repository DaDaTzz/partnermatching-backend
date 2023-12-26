package com.da.usercenter.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户消息
 */
@Data
public class UserMessageVO {
    private Long userId;
    private String nickname;
    private String profilePhoto;
    private String message;
    private Date sendTime;

}

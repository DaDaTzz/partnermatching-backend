package com.da.usercenter.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加好友请求类
 */
@Data
public class AddFollowRequest implements Serializable {
    private static final long serialVersionUID = -3240214264289444792L;
    /**
     * 用户id
     */
    private Long id;
}

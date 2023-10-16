package com.da.usercenter.model.dto.user;

import lombok.Data;

/**
 * 修改密码请求类
 */
@Data
public class UpdatePasswordRequest {
    /**
     * 账号
     */
    private String loginAccount;

    /**
     * 新的密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 验证码
     */
    private String inputCode;

}

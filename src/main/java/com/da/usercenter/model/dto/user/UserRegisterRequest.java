package com.da.usercenter.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Da
 * &#064;date  2023/6/10 19:22
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3114662314076468474L;

    /**
     * 登录账户
     */
    private String loginAccount;

    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 二次输入密码
     */
    private String checkPassword;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 输入的验证码
     */
    private String inputCode;

}

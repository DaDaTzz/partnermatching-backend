package com.da.usercenter.model.dto.user;

import lombok.Data;

/**
 * 发送短信验证码
 */
@Data
public class SendSmsRequest {
    private String phone;
}

package com.da.usercenter.model.dto.team;

import lombok.Data;

/**
 * 绑定邮箱请求
 */
@Data
public class BindPhoneRequest {
    private Long id;
    private String phone;
    private String inputCode;
}

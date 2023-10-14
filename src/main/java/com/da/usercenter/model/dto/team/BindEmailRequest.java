package com.da.usercenter.model.dto.team;

import lombok.Data;

/**
 * 绑定邮箱请求
 */
@Data
public class BindEmailRequest {
    private Long id;
    private String receiveEmail;
    private String inputCode;
}

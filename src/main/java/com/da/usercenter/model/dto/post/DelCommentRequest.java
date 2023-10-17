package com.da.usercenter.model.dto.post;

import lombok.Data;

@Data
public class DelCommentRequest {
    private Long createUserId;
    private Long id;
}

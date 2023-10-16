package com.da.usercenter.model.dto.postcomment;

import lombok.Data;

/**
 * 添加评论请求
 */
@Data
public class AddCommentRequest {
    private Long postId;
    private String content;
}

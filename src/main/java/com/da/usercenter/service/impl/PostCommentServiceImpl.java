package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.PostComment;
import com.da.usercenter.service.PostCommentService;
import com.da.usercenter.mapper.PostCommentMapper;
import org.springframework.stereotype.Service;

/**
* @author 达
* @description 针对表【post_comment(帖子评论)】的数据库操作Service实现
* @createDate 2023-10-16 16:03:03
*/
@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment>
    implements PostCommentService{

}





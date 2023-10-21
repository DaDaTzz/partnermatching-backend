package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.model.entity.UserMessage;
import com.da.usercenter.service.UserMessageService;
import com.da.usercenter.mapper.UserMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author 达
* @description 针对表【user_message(用户消息)】的数据库操作Service实现
* @createDate 2023-10-21 21:41:56
*/
@Service
public class UserMessageServiceImpl extends ServiceImpl<UserMessageMapper, UserMessage>
    implements UserMessageService{

}





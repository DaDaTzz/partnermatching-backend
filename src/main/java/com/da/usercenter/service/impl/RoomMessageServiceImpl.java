package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.model.entity.RoomMessage;
import com.da.usercenter.service.RoomMessageService;
import com.da.usercenter.mapper.RoomMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author 达
* @description 针对表【room_message(聊天室消息)】的数据库操作Service实现
* @createDate 2023-10-21 21:41:52
*/
@Service
public class RoomMessageServiceImpl extends ServiceImpl<RoomMessageMapper, RoomMessage>
    implements RoomMessageService{

}





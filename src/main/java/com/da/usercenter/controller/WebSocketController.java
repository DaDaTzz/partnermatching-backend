package com.da.usercenter.controller;

import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.component.WebSocket;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.RoomMessage;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.vo.UserMessageVO;
import com.da.usercenter.service.RoomMessageService;
import com.da.usercenter.service.UserMessageService;
import com.da.usercenter.service.UserService;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ws")
public class WebSocketController {

    @Resource
    private WebSocket webSocket;
    @Resource
    private UserService userService;
    @Resource
    private RoomMessageService roomMessageService;
    @Resource
    private UserMessageService userMessageService;

    @GetMapping("/publicRoom")
    public void publicRoom(String msg, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        UserMessageVO userMessageVO = new UserMessageVO();
        userMessageVO.setUserId(currentUser.getId());
        userMessageVO.setNickname(currentUser.getNickname());
        userMessageVO.setProfilePhoto(currentUser.getProfilePhoto());
        userMessageVO.setMessage(msg);
        // 插入数据库
        RoomMessage roomMessage = new RoomMessage();
        roomMessage.setUserId(currentUser.getId());
        roomMessage.setUserAwata(currentUser.getProfilePhoto());
        roomMessage.setMessage(msg);
        roomMessage.setNickname(currentUser.getNickname());
        roomMessageService.save(roomMessage);

        Gson gson = new Gson();
        String userMsgGson = gson.toJson(userMessageVO);
        webSocket.sendMessage(userMsgGson);

    }
}

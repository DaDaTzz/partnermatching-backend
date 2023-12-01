package com.da.usercenter.controller;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.component.WebSocket;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.*;
import com.da.usercenter.model.vo.TeamUserVO;
import com.da.usercenter.model.vo.UserMessageVO;
import com.da.usercenter.model.vo.UserVO;
import com.da.usercenter.service.*;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/message")
public class WebSocketController {

    @Resource
    private WebSocket webSocket;
    @Resource
    private UserService userService;
    @Resource
    private RoomMessageService roomMessageService;
    @Resource
    private UserMessageService userMessageService;
    @Resource
    private UserTeamService userTeamService;


    /**
     * 公共聊天室
     * @param msg
     * @param request
     */
    @GetMapping("/publicRoom")
    public void publicRoom(String msg, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if(StringUtils.isBlank(msg)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        UserMessageVO userMessageVO = new UserMessageVO();
        userMessageVO.setUserId(currentUser.getId());
        userMessageVO.setNickname(currentUser.getNickname());
        userMessageVO.setProfilePhoto(currentUser.getProfilePhoto());
        userMessageVO.setMessage(msg);
        // 插入数据库
        RoomMessage roomMessage = new RoomMessage();
        roomMessage.setUserId(currentUser.getId());
        roomMessage.setMessage(msg);
        roomMessageService.save(roomMessage);

        Gson gson = new Gson();
        String userMsgGson = gson.toJson(userMessageVO);
        webSocket.sendMessage(userMsgGson);

    }

    /**
     * 公共房间历史聊天记录
     * @param request
     * @return
     */
    @GetMapping("/publicRoomOldMessage")
    public ResponseResult<List<UserMessageVO>> getPublicRoomMessage(HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<RoomMessage> roomMessageList = roomMessageService.lambdaQuery().isNull(RoomMessage::getTeamid).list();
        if(roomMessageList.size() == 0){
            return null;
        }
        List<UserMessageVO> userMessageVOS = new ArrayList<>();
        for (RoomMessage roomMessage : roomMessageList) {
            UserMessageVO userMessageVO = new UserMessageVO();
            userMessageVO.setUserId(roomMessage.getUserId());
            User user = userService.getById(roomMessage.getUserId());
            userMessageVO.setNickname(user.getNickname());
            userMessageVO.setProfilePhoto(user.getProfilePhoto());
            userMessageVO.setMessage(roomMessage.getMessage());
            userMessageVOS.add(userMessageVO);
        }
        return ResponseResult.success(userMessageVOS);
    }

    /**
     * 队伍聊天室
     * @param request
     */
    @GetMapping("/teamRoom")
    public void teamRoom(long teamId,String msg, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if(StringUtils.isBlank(msg)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 判断是否为当前队伍成员
        List<UserTeam> userTeamList = userTeamService.lambdaQuery().eq(UserTeam::getTeamId, teamId).list();
        ArrayList<Long> joinUsersId = new ArrayList<>();
        if(userTeamList.size() > 0){
            for (UserTeam userTeam : userTeamList) {
                joinUsersId.add(userTeam.getUserId());
            }
        }
        if(!joinUsersId.contains(currentUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH,"不是本队伍成员");
        }

        UserMessageVO userMessageVO = new UserMessageVO();
        userMessageVO.setUserId(currentUser.getId());
        userMessageVO.setNickname(currentUser.getNickname());
        userMessageVO.setProfilePhoto(currentUser.getProfilePhoto());
        userMessageVO.setMessage(msg);
        // 插入数据库
        RoomMessage roomMessage = new RoomMessage();
        roomMessage.setTeamid(teamId);
        roomMessage.setUserId(currentUser.getId());
        roomMessage.setMessage(msg);
        roomMessageService.save(roomMessage);
        Gson gson = new Gson();
        String userMsgGson = gson.toJson(userMessageVO);
        webSocket.sendMessage(userMsgGson);

    }

    /**
     * 队伍房间历史聊天记录
     * @param request
     * @return
     */
    @GetMapping("/teamRoomOldMessage")
    public ResponseResult<List<UserMessageVO>> getTeamRoomMessage(long teamId,HttpServletRequest request ){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 判断是否为当前队伍成员
        List<UserTeam> userTeamList = userTeamService.lambdaQuery().eq(UserTeam::getTeamId, teamId).list();
        ArrayList<Long> joinUsersId = new ArrayList<>();
        if(userTeamList.size() > 0){
            for (UserTeam userTeam : userTeamList) {
                joinUsersId.add(userTeam.getUserId());
            }
        }
        if(!joinUsersId.contains(currentUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH,"不是本队伍成员");
        }
        List<RoomMessage> roomMessageList = roomMessageService.lambdaQuery().eq(RoomMessage::getTeamid,teamId).list();
        if(roomMessageList.size() == 0){
            return null;
        }
        List<UserMessageVO> userMessageVOS = new ArrayList<>();
        for (RoomMessage roomMessage : roomMessageList) {
            UserMessageVO userMessageVO = new UserMessageVO();
            userMessageVO.setUserId(roomMessage.getUserId());
            User user = userService.getById(roomMessage.getUserId());
            userMessageVO.setNickname(user.getNickname());
            userMessageVO.setProfilePhoto(user.getProfilePhoto());
            userMessageVO.setMessage(roomMessage.getMessage());
            userMessageVOS.add(userMessageVO);
        }
        return ResponseResult.success(userMessageVOS);
    }


    /**
     * 私聊
     * @param request
     */
    @GetMapping("/privateRoom")
    public void privateRoom(long toId,String msg, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if(StringUtils.isBlank(msg)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        UserMessageVO userMessageVO = new UserMessageVO();
        userMessageVO.setUserId(currentUser.getId());
        userMessageVO.setNickname(currentUser.getNickname());
        userMessageVO.setProfilePhoto(currentUser.getProfilePhoto());
        userMessageVO.setMessage(msg);
        // 插入数据库
        UserMessage userMessage = new UserMessage();
        userMessage.setFromId(currentUser.getId());
        userMessage.setToId(toId);
        userMessage.setMessage(msg);
        userMessageService.save(userMessage);

        Gson gson = new Gson();
        String userMsgGson = gson.toJson(userMessageVO);
        webSocket.sendMessage(userMsgGson);

    }

    /**
     * 队伍房间历史聊天记录
     * @param request
     * @return
     */
    @GetMapping("/privateRoomOldMessage")
    public ResponseResult<List<UserMessageVO>> getPrivateRoomMessage(long toId,HttpServletRequest request ){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<UserMessage> userMessageList = userMessageService.lambdaQuery().eq(UserMessage::getFromId, currentUser.getId()).eq(UserMessage::getToId, toId).or().eq(UserMessage::getToId, currentUser.getId()).eq(UserMessage::getFromId, toId).list();
        if(userMessageList.size() == 0){
            return null;
        }
        List<UserMessageVO> userMessageVOS = new ArrayList<>();
        for (UserMessage userMessage : userMessageList) {
            UserMessageVO userMessageVO = new UserMessageVO();
            userMessageVO.setUserId(userMessage.getFromId());
            User user = userService.getById(userMessage.getFromId());
            userMessageVO.setNickname(user.getNickname());
            userMessageVO.setProfilePhoto(user.getProfilePhoto());
            userMessageVO.setMessage(userMessage.getMessage());
            userMessageVOS.add(userMessageVO);
        }
        return ResponseResult.success(userMessageVOS);
    }
}

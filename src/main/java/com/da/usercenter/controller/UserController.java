package com.da.usercenter.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.request.*;
import com.da.usercenter.model.vo.UserVO;
import com.da.usercenter.service.UserService;
import com.da.usercenter.utils.TokenUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户接口
 *
 * @author 达
 */
@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = {"http://8.130.133.165"},allowCredentials = "true")
@CrossOrigin(origins = {"http://127.0.0.1:5173"},allowCredentials = "true")
public class UserController{
    @Resource
    private UserService userService;


    /**
     * 注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public ResponseResult<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long res = userService.userRegister(userRegisterRequest.getLoginAccount(),userRegisterRequest.getLoginPassword(), userRegisterRequest.getCheckPassword(),userRegisterRequest.getNickname());
        return ResponseResult.success(res, "注册成功！");
    }

    /**
     * 登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ResponseResult<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String loginAccount = userLoginRequest.getLoginAccount();
        String loginPassword = userLoginRequest.getLoginPassword();
        if(StrUtil.isAllBlank(loginAccount, loginPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User safeUser = userService.userLogin(loginAccount, loginPassword, request);
        if(safeUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 返回token
        String token = TokenUtils.getToken(String.valueOf(safeUser.getId()));
        return ResponseResult.success(safeUser, token);
    }

    /**
     * 注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public ResponseResult<Boolean> userLogOut(HttpServletRequest request){
        Boolean result = userService.userLogOut(request);
        return ResponseResult.success(result, "注销成功");
    }

    /**
     * 根据条件查询用户信息
     * @param nickName
     * @param request
     * @return
     */
    @GetMapping("/search")
    public ResponseResult<List<User>> searchUser(String nickName, HttpServletRequest request){
        List<User> result = userService.searchUser(nickName, request);
        return ResponseResult.success(result);
    }

    /**
     * 推荐用户
     * @param pageSize
     * @param pageNum
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public ResponseResult<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){
        Page<User> userPage = userService.recommendUsers(pageSize, pageNum, request);
        return ResponseResult.success(userPage);
    }


    /**
     * 获取当前用户信息
     * @param request
     * @return
     */
    @GetMapping("/current")
    public ResponseResult<User> getCurrentUser(HttpServletRequest request){
        User result = userService.getCurrentUser(request);
        return ResponseResult.success(result);
    }

    /**
     * 更新用户信息
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> updateUser(@RequestBody User user,HttpServletRequest request){
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean res = userService.updateUser(user, request);
        return ResponseResult.success(res);
    }

    /**
     * 通过标签查询用户信息
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public ResponseResult<List<User>> getUserByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResponseResult.success(userList);
    }

    /**
     * 匹配相似用户
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public ResponseResult<List<User>> matchUsers(long num, String nickname,HttpServletRequest request){
        List<User> userList = userService.matchUsers(num, nickname,request);
        return ResponseResult.success(userList);
    }

    /**
     * 获取已关注的用户列表
     * @param request
     * @return
     */
    @GetMapping("/get/love")
    public ResponseResult<List<UserVO>> getLoves(HttpServletRequest request){
        List<UserVO> lovesList = userService.getLoves(request);
        return ResponseResult.success(lovesList);
    }

    @GetMapping("/get/fans")
    public ResponseResult<List<UserVO>> getFans(HttpServletRequest request){
        List<UserVO> fansList = userService.getFans(request);
        return ResponseResult.success(fansList);
    }


    /**
     * 关注 / 取关
     * @param addFansRequest
     * @param request
     * @return
     */
    @PostMapping("/addLove")
    public ResponseResult<Boolean> addLove(@RequestBody AddLoveRequest addFansRequest, HttpServletRequest request){
        Boolean res = userService.addLove(addFansRequest, request);
        return ResponseResult.success(res);
    }


    /**
     * 更新标签
     * @param updateTagRequest
     * @param request
     * @return
     */
    @PostMapping("/updateTag")
    public ResponseResult<Boolean> updateTag(@RequestBody UpdateTagRequest updateTagRequest,HttpServletRequest request){
        Boolean res = userService.updateTag(updateTagRequest, request);
        return ResponseResult.success(res);
    }


}


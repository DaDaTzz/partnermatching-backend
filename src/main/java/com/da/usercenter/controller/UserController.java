package com.da.usercenter.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.manager.RedisLimiterManager;
import com.da.usercenter.model.dto.team.BindPhoneRequest;
import com.da.usercenter.model.dto.team.SendMailRequest;
import com.da.usercenter.model.dto.user.*;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.vo.UserVO;
import com.da.usercenter.service.UserService;
import com.da.usercenter.utils.IpUtils;
import com.da.usercenter.utils.SMSUtils;
import com.da.usercenter.utils.TokenUtils;
import com.da.usercenter.utils.ValidateCodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 用户接口
 *
 * @author 达
 */
@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = {"http://8.130.133.165"},allowCredentials = "true")
//@CrossOrigin(origins = {"http://127.0.0.1:5173"}, allowCredentials = "true")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private JavaMailSender javaMailSender;

    /**
     * 发送邮件的邮箱
     */
    private String SEND_EMAIL = "1349190697@qq.com";


    /**
     * 注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public ResponseResult<Boolean> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long userId = userService.userRegister(userRegisterRequest.getLoginAccount(), userRegisterRequest.getLoginPassword(), userRegisterRequest.getCheckPassword(), userRegisterRequest.getNickname(), userRegisterRequest.getEmail(), userRegisterRequest.getInputCode());
        if (userId > 0) {
            // 返回token
            String token = TokenUtils.getToken(String.valueOf(userId));
            return ResponseResult.success(true, token);
        }
        return ResponseResult.success(false);

    }

    /**
     * 登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ResponseResult<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String loginAccount = userLoginRequest.getLoginAccount();
        String loginPassword = userLoginRequest.getLoginPassword();
        if (StrUtil.isAllBlank(loginAccount, loginPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User safeUser = userService.userLogin(loginAccount, loginPassword, request);
        if (safeUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 返回token
        String token = TokenUtils.getToken(String.valueOf(safeUser.getId()));
        return ResponseResult.success(safeUser, token);
    }

    /**
     * 注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public ResponseResult<Boolean> userLogOut(HttpServletRequest request) {
        Boolean result = userService.userLogOut(request);
        return ResponseResult.success(result, "注销成功");
    }

    /**
     * 根据条件查询用户信息
     *
     * @param nickName
     * @param request
     * @return
     */
    @GetMapping("/search")
    public ResponseResult<List<User>> searchUser(String nickName, HttpServletRequest request) {
        List<User> result = userService.searchUser(nickName, request);
        return ResponseResult.success(result);
    }

    /**
     * 推荐用户
     *
     * @param pageSize
     * @param pageNum
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public ResponseResult<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        Page<User> userPage = userService.recommendUsers(pageSize, pageNum, request);
        return ResponseResult.success(userPage);
    }


    /**
     * 获取当前用户信息
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public ResponseResult<User> getCurrentUser(HttpServletRequest request) {
        User result = userService.getCurrentUser(request);
        return ResponseResult.success(result);
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean res = userService.updateUser(user, request);
        return ResponseResult.success(res);
    }

    /**
     * 通过标签查询用户信息
     *
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public ResponseResult<List<User>> getUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResponseResult.success(userList);
    }

    /**
     * 匹配相似用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public ResponseResult<List<UserVO>> matchUsers(long num, String nickname, HttpServletRequest request) {
        List<UserVO> userList = userService.matchUsers(num, nickname, request);
        return ResponseResult.success(userList);
    }

    /**
     * 获取已关注的用户列表
     *
     * @param request
     * @return
     */
    @GetMapping("/get/love")
    public ResponseResult<List<UserVO>> getLoves(HttpServletRequest request) {
        List<UserVO> lovesList = userService.getLoves(request);
        return ResponseResult.success(lovesList);
    }

    /**
     * 获取粉丝列表
     *
     * @param request
     * @return
     */
    @GetMapping("/get/fans")
    public ResponseResult<List<UserVO>> getFans(HttpServletRequest request) {
        List<UserVO> fansList = userService.getFans(request);
        return ResponseResult.success(fansList);
    }


    /**
     * 关注 / 取关
     *
     * @param addFansRequest
     * @param request
     * @return
     */
    @PostMapping("/follow")
    public ResponseResult<Boolean> addFollow(@RequestBody AddFollowRequest addFansRequest, HttpServletRequest request) {
        Boolean res = userService.addFollow(addFansRequest, request);
        return ResponseResult.success(res);
    }


    /**
     * 更新标签
     *
     * @param updateTagRequest
     * @param request
     * @return
     */
    @PostMapping("/updateTag")
    public ResponseResult<Boolean> updateTag(@RequestBody UpdateTagRequest updateTagRequest, HttpServletRequest request) {
        Boolean res = userService.updateTag(updateTagRequest, request);
        return ResponseResult.success(res);
    }


    /**
     * 发送短信验证码
     *
     * @param sendSmsRequest
     * @return
     */
    @PostMapping("/sendSms")
    public ResponseResult<Boolean> SendSms(@RequestBody SendSmsRequest sendSmsRequest, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        String phone = sendSmsRequest.getPhone();
        if (StringUtils.isBlank(phone)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "手机号为空");
        }
        String regex = "^(1[3456789]\\d{9})$";
        if (!phone.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
        }
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        // 调用阿里云api发送短信验证码
        System.out.println("生成的验证码为：" + code);
        // 限流判断（根据 ip）
        String ipAddress = IpUtils.getIpAddress(request);
        redisLimiterManager.doRateLimit("sendSms:" + ipAddress);
        SMSUtils.sendMessage("组队鸭", "SMS_463612945", phone, code);
        // 使用redis缓存短信验证码,设置有效时间
        redisTemplate.opsForValue().set("sendSms:" + phone, code, 5L, TimeUnit.MINUTES);
        return ResponseResult.success(true);
    }


    /**
     * 发送邮箱验证码
     *
     * @param sendMailRequest
     * @return
     */
    @PostMapping("/sendEmail")
    public ResponseResult<Boolean> sendEmail(@RequestBody SendMailRequest sendMailRequest, HttpServletRequest request) {
        String receiveEmail = sendMailRequest.getReceiveEmail();
        if (StringUtils.isBlank(receiveEmail)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "收件箱为空");
        }
        // 只支持QQ邮箱
        String regex = "^[a-zA-Z0-9_\\-]+@qq\\.com$";
        if (!receiveEmail.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持QQ邮箱");
        }
        // 限流判断（根据 ip）
        String ipAddress = IpUtils.getIpAddress(request);
        redisLimiterManager.doRateLimit("sendEmail:" + ipAddress);
        StringBuffer code = new StringBuffer();// 验证码
        Random random = new Random();
        int randomNumber = random.nextInt(9000) + 1000;
        code.append(randomNumber);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("验证码"); // 发送邮件的标题
        message.setText("验证码：" + code + "，切勿将验证码泄露给他人，本条验证码有效期5分钟。"); // 发送邮件的内容
        message.setTo(receiveEmail); // 指定要接收邮件的用户邮箱账号
        message.setFrom(SEND_EMAIL); // 发送邮件的邮箱账号，注意一定要和配置文件中的一致！
        try {
            javaMailSender.send(message);// 调用send方法发送邮件即可
        } catch (MailException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误!");
        }
        // 使用redis缓存短信验证码,设置有效时间
        redisTemplate.opsForValue().set("sendEmail:" + receiveEmail, code, 5L, TimeUnit.MINUTES);
        return ResponseResult.success(true);
    }

    /**
     * 绑定手机
     *
     * @param bindPhoneRequest
     * @param request
     * @return
     */
    @PostMapping("/bindPhone")
    public ResponseResult<Boolean> bindPhone(@RequestBody BindPhoneRequest bindPhoneRequest, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }
        Long id = currentUser.getId();
        String phone = bindPhoneRequest.getPhone();
        String inputCode = bindPhoneRequest.getInputCode();
        if (StringUtils.isAnyBlank(id.toString(), phone, inputCode)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "手机号为空");
        }
        String code = redisTemplate.opsForValue().get("sendSms:" + phone).toString();
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机验证码已过期");
        }
        if (!code.equals(inputCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机验证码错误");
        }
        // 一个手机只能绑定一个账号
        Integer count = userService.lambdaQuery().eq(User::getPhone, phone).count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该手机已绑定其他账户");
        }
        User user = new User();
        user.setId(currentUser.getId());
        user.setPhone(phone);
        boolean res = userService.updateById(user);
        // 删除 redis 中的验证码信息
        if (res) {
            redisTemplate.delete("sendSms:" + phone);
        }
        // 执行 update 操作后，更新 redis 中的用户信息
        redisTemplate.delete("user:login:" + user.getId());
        User u = userService.getById(id);
        User safeUser = userService.getSafeUser(u);
        redisTemplate.opsForValue().set("user:login:" + id, safeUser, 30, TimeUnit.MINUTES);
        return ResponseResult.success(res);


    }

    /**
     * 修改密码
     *
     * @param updatePasswordRequest
     * @return
     */
    @PostMapping("/updatePassword")
    public ResponseResult<Boolean> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        String newPassword = updatePasswordRequest.getNewPassword();
        String email = updatePasswordRequest.getEmail();
        String inputCode = updatePasswordRequest.getInputCode();
        String loginAccount = updatePasswordRequest.getLoginAccount();
        String checkPassword = updatePasswordRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(newPassword, email, inputCode, loginAccount, checkPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数为空");
        }
        Boolean res = userService.updatePassword(newPassword, email, inputCode, loginAccount, checkPassword);
        return ResponseResult.success(res);

    }

    /**
     * 每日签到
     *
     * @param request
     * @return
     */
    @PostMapping("/sign")
    public ResponseResult<Boolean> sign(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 不能重复签到
        if (currentUser.getSign() == 1) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "今日已完成签到！");
        }
        User user = new User();
        user.setId(currentUser.getId());
        user.setSign(1);
        // 签到成功 +10 积分
        user.setIntegral(currentUser.getIntegral() + 10);
        boolean res = userService.updateById(user);
        if(res){
            // 更新缓存
            User u = userService.getById(currentUser.getId());
            User safeUser = userService.getSafeUser(u);
            ValueOperations valueOperations = redisTemplate.opsForValue();
            valueOperations.set("user:login:" + safeUser.getId(), safeUser, 30, TimeUnit.MINUTES);
            return ResponseResult.success(true);
        }
        return ResponseResult.success(false);
    }





}


package com.da.usercenter.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.mapper.UserFollowsMapper;
import com.da.usercenter.mapper.UserMapper;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.entity.UserFollows;
import com.da.usercenter.model.dto.user.AddFollowRequest;
import com.da.usercenter.model.dto.user.UpdateTagRequest;
import com.da.usercenter.model.vo.UserVO;
import com.da.usercenter.service.UserFollowsService;
import com.da.usercenter.service.UserService;
import com.da.usercenter.utils.AlgorithmUtil;
import com.da.usercenter.utils.TokenUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.da.usercenter.common.ErrorCode.*;
import static com.da.usercenter.constant.UserConstant.*;

/**
 * (User)表服务实现类
 *
 * @author Da
 * @since 2023-06-10 13:40:21
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserFollowsService userFollowsService;

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "Da";

    @Resource
    private UserFollowsMapper userFriendMapper;
    @Resource
    private UserMapper userMapper;


    /**
     * 注册
     *
     * @param loginAccount  账号
     * @param loginPassword 密码
     * @param checkPassword 校验密码
     * @return 创建成功的用户 id
     */
    @Override
    public Long userRegister(String loginAccount, String loginPassword, String checkPassword, String nickname, String email, String inputCode) {
        // 非空校验
        if (org.apache.commons.lang3.StringUtils.isAnyBlank(loginAccount, loginPassword, checkPassword,nickname, email, inputCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于6位
        if (loginAccount.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度小于6位");
        }
        // 密码不小于8位
        if (loginPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度小于8位");
        }
        // 账户不能包含特殊字符
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        if (p.matcher(loginAccount).find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 密码和校验密码相同
        if (!loginPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不同");
        }
        // 昵称不为空
        if (StringUtils.isBlank(nickname)) {
            throw new BusinessException(NULL_ERROR, "昵称能为空");
        }
        // 账号不能重复
        Integer count = this.lambdaQuery().eq(User::getLoginAccount, loginAccount).count();
        if (count > 0) {
            throw new BusinessException(PARAMS_ERROR, "账户名已存在");
        }
        // 一个邮箱只能绑定一个账号
        Integer count1 = this.lambdaQuery().eq(User::getEmail, email).count();
        if (count1 > 0) {
            throw new BusinessException(PARAMS_ERROR, "该邮箱已注册，请直接登录!");
        }
        // 校验邮箱验证码
        String code = redisTemplate.opsForValue().get("sendEmail:" + email).toString();
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(PARAMS_ERROR, "验证码已过期");
        }
        if (!code.equals(inputCode)) {
            throw new BusinessException(PARAMS_ERROR, "验证码错误");
        }
        // 密码加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + loginPassword).getBytes());
        // 插入数据
        User user = new User();
        user.setLoginAccount(loginAccount);
        user.setLoginPassword(newPassword);
        user.setNickname(nickname);
        user.setEmail(email);
        // 默认头像
        user.setProfilePhoto("https://5b0988e595225.cdn.sohucs.com/q_70,c_zoom,w_640/images/20180616/186872ef63844c58876783931c66dddd.gif");
        // 默认简介
        user.setProfile("这个人很懒，什么都没留下！");
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "注册失败，未知原因");
        }
        // 删除 redis 中的验证码信息
        redisTemplate.delete("sendEmail:" + email);
        User newUser = this.lambdaQuery().eq(User::getLoginAccount, loginAccount).one();
        User safeUser = this.getSafeUser(newUser);
        // 将用户信息存入redis
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("user:login:" + newUser.getId(), safeUser, 30, TimeUnit.MINUTES);
        return newUser.getId();
    }


    /**
     * 登录
     *
     * @param loginAccount  账号
     * @param loginPassword 密码
     * @param request       客户端请求对象
     * @return 登录用户信息
     */
    @Override
    public User userLogin(String loginAccount, String loginPassword, HttpServletRequest request) {
        // 非空校验
        if (StrUtil.isAllBlank(loginAccount, loginPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于6位
        if (loginAccount.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号小于6位");
        }
        // 密码不小于8位
        if (loginPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码小于8位");
        }
        // 账户不能包含特殊字符
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        if (p.matcher(loginAccount).find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 密码加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + loginPassword).getBytes());
        // 校验密码是否输入正确
        User user = this.lambdaQuery().eq(User::getLoginAccount, loginAccount).eq(User::getLoginPassword, newPassword).one();
        if (user == null) {
            log.info("loginAccount not matcher loginPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号和密码不匹配");
        }
        User safeUser = this.getSafeUser(user);
        // 将登录信息存入redis 单点登录
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("user:login:" + user.getId(), safeUser, 30, TimeUnit.MINUTES);
        return safeUser;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String userId = TokenUtils.getAccount(token);
        // 从缓存中获取用户信息
        User user = (User) redisTemplate.opsForValue().get("user:login:" + userId);
        if (user == null) {
            return null;
        }
        if (user != null && USER_DISABLE.equals(user.getStates())) {
            return null;
        }
        return this.getSafeUser(user);
    }

    /**
     * 通过昵称查询用户信息
     *
     * @param nickName 昵称
     * @param request  客户端请求对象
     * @return userList
     */
    @Override
    public List<User> searchUser(String nickName, HttpServletRequest request) {
        // 权限校验
        if (!isAdmin(request)) {
            throw new BusinessException(NO_AUTH);
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(nickName)) {
            queryWrapper.like(User::getNickname, nickName);
        }
        List<User> userList = this.list(queryWrapper);
        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());
    }


    /**
     * 判断是否为管理员
     *
     * @param request 客户端请求对象
     * @return boolean
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User user = this.getCurrentUser(request);
        if (user == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        // 权限校验
        return ADMIN_USER.equals(user.getType());
    }

    /**
     * 判断是否为管理员
     *
     * @param loginUser 已登录用户信息
     * @return boolean
     */
    @Override
    public boolean isAdmin(User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        return ADMIN_USER.equals(loginUser.getType());
    }


    @Override
    public User getSafeUser(User user) {
        if (user == null) {
            return null;
        }
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setNickname(user.getNickname());
        safeUser.setLoginAccount(user.getLoginAccount());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setSex(user.getSex());
        safeUser.setStates(user.getStates());
        safeUser.setProfilePhoto(user.getProfilePhoto());
        safeUser.setCreateTime(user.getCreateTime());
        safeUser.setType(user.getType());
        safeUser.setTags(user.getTags());
        safeUser.setProfile(user.getProfile());
        return safeUser;
    }

    /**
     * 获取当前用户信息
     *
     * @param request 客户端请求对象
     * @return 当前用户信息
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentUser = (User) userObj;
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(NOT_LOGIN, "未登录");
        }
        String userId = TokenUtils.getAccount(token);
        // 从缓存中获取用户信息
        User user = (User) redisTemplate.opsForValue().get("user:login:" + userId);
        if (user == null) {
            throw new BusinessException(LOGIN_EXPIRE);
        }
        if (user != null && USER_DISABLE.equals(user.getStates())) {
            throw new BusinessException(USER_STATE_ERROR, "账号已被封禁");
        }
        return this.getSafeUser(user);
    }

    /**
     * 注销
     *
     * @param request 客户端请求对象
     * @return 1-注销成功
     */
    @Override
    public Boolean userLogOut(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(NULL_ERROR);
        }
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(NOT_LOGIN);
        }
        if (!TokenUtils.verify(token)) {
            throw new BusinessException(LOGIN_EXPIRE);
        }
        String userId = TokenUtils.getAccount(token);
        // 移除 redis 中的用户信息
        Boolean res = redisTemplate.delete("user:login:" + userId);
        return res;
    }

    /**
     * 通过标签搜索用户信息
     *
     * @param tagNameList 标签 list
     * @return userList
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // ArrayList<User> users = new ArrayList<>();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper.like(User::getTags, tagName);
        }
        List<User> userList = this.list(queryWrapper);
        ArrayList<User> safeUsers = new ArrayList<>();
        for (User user : userList) {
            User safeUser = this.getSafeUser(user);
            safeUsers.add(safeUser);
        }
        return safeUsers;
    }


    /**
     * 更新用户
     *
     * @param user 更新用户信息
     * @return boolean
     */
    @Override
    public Boolean updateUser(User user, HttpServletRequest request) {
        // 1.校验参数是否为空
        if (user == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        // 2.权限校验
        User loginUser = this.getCurrentUser(request);
        // 校验是否为管理员或自己
        if (isAdmin(loginUser) || loginUser.getId() == user.getId()) {
            boolean res = this.updateById(user);
            if (res) {
                // 昵称不能为空
                if (StringUtils.isBlank(user.getNickname())) {
                    user.setNickname("无名氏");
                    User newUserInfo = this.getById(user.getId());
                    User safeUser = this.getSafeUser(newUserInfo);
                    redisTemplate.opsForValue().set("user:login:" + user.getId(), safeUser, 30, TimeUnit.MINUTES);
                }
            }
            // 执行更新操作后删除 redis 中的缓存数据， 保证数据的一致性
            redisTemplate.delete("user:login:" + user.getId());
            long id = loginUser.getId();
            User u = this.getById(id);
            User safeUser = this.getSafeUser(u);
            redisTemplate.opsForValue().set("user:login:" + user.getId(), safeUser);
            // 删除推荐用户列表缓存 防止数据不统一
            Set<String> keys = redisTemplate.keys("user:recommend:" + "*");
            redisTemplate.delete(keys);
            return res;
        }
        if (loginUser.getId() != user.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "没有权限");
        }
        return false;
    }

    /**
     * 推荐用户
     *
     * @param request 客户端请求对象
     * @return 推荐用户分页对象
     */
    @Override
    public Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        // 如果有缓存，直接从缓存中读取用户信息
        User loginUser = null;
        try {
            loginUser = this.getCurrentUser(request);
        } catch (Exception e) {
            log.error("user login error", e);
        }
        long userId = 0;
        if (loginUser != null) {
            userId = loginUser.getId();
        }
        String redisKey = "user:recommend:" + userId;
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return userPage;
        }
        // 无缓存，查询数据库
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        userPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 将读到的数据存入缓存中
        try {
            valueOperations.set(redisKey, userPage, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return userPage;
    }

    /**
     * 匹配相似用户
     * @param num 推荐数量
     * @param nickname
     * @param request 客户端请求对象
     * @return
     */
    @Override
    public List<UserVO> matchUsers(long num, String nickname, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(PARAMS_ERROR);
        }
        User loginUser = this.getCurrentUser(request);
        if (loginUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        if (loginUser.getTags() == null) {
            throw new BusinessException(PARAMS_ERROR, "无标签，无法匹配");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(User::getId, User::getTags);
        queryWrapper.isNotNull(User::getTags);
        if (StringUtils.isNotBlank(nickname)) {
            queryWrapper.like(User::getNickname, nickname);
        }
        List<User> userList = this.list(queryWrapper);
        if(userList.size() == 0){
            return new ArrayList<>();
        }
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtil.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.in(User::getId, userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafeUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        // 是否关注
        List<UserVO> loves = this.getLoves(request);
        ArrayList<Long> loveIdList = new ArrayList<>();
        for (int i = 0; i < loves.size(); i++) {
            loveIdList.add(loves.get(i).getId());
        }
        ArrayList<UserVO> userVOS = new ArrayList<>();
        for (int i = 0; i < finalUserList.size(); i++) {
            User user = finalUserList.get(i);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            if (loveIdList.contains(user.getId())) {
                userVO.setIsFollow(true);
            }else{
                userVO.setIsFollow(false);
            }
            userVOS.add(userVO);
        }
        return userVOS;
    }

    @Override
    public List<UserVO> getLoves(HttpServletRequest request) {
        // 非空
        if (request == null) {
            throw new BusinessException(NULL_ERROR);
        }
        // 是否登录
        User currentUser = this.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        List<User> loves = userFriendMapper.getLovesByUserId(currentUser.getId());
        ArrayList<UserVO> loveList = new ArrayList<>();

        // 用户信息脱敏
        for (User love : loves) {
            UserVO userVO = new UserVO();
            User safeUser = this.getSafeUser(love);
            BeanUtils.copyProperties(safeUser, userVO);
            userVO.setIsFollow(true);
            loveList.add(userVO);
        }
        return loveList;

    }


    /**
     * 关注/取关
     *
     * @param addFriendRequest
     * @param request
     * @return
     */
    @Override
    public Boolean addFollow(AddFollowRequest addFriendRequest, HttpServletRequest request) {
        if (addFriendRequest == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        User currentUser = this.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        Long id = addFriendRequest.getId();
        // 不能关注自己
        if (id == currentUser.getId()) {
            throw new BusinessException(PARAMS_ERROR, "不能关注自己！");
        }
        long userId = currentUser.getId();
        LambdaQueryWrapper<UserFollows> userFriendLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFriendLambdaQueryWrapper.eq(UserFollows::getUserId, userId).eq(UserFollows::getLoveId, id);
        UserFollows one = userFollowsService.getOne(userFriendLambdaQueryWrapper);
        if (one != null) {
            Integer isFollow = one.getIsFollow();
            if (isFollow == 1) {
                one.setIsFollow(0);
                return userFollowsService.updateById(one);
            } else if (isFollow == 0) {
                one.setIsFollow(1);
                return userFollowsService.updateById(one);
            }
        }
        // 插入数据
        UserFollows userFollows = new UserFollows();
        userFollows.setUserId(userId);
        userFollows.setLoveId(id);
        userFollows.setIsFollow(1);
        return userFollowsService.save(userFollows);
    }


    /**
     * 更新标签
     *
     * @param updateTagRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateTag(UpdateTagRequest updateTagRequest, HttpServletRequest request) {
        User currentUser = this.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(NOT_LOGIN, "未登录");
        }
        String[] tags = updateTagRequest.getTags();
        if (tags == null || tags.length == 0) {
            throw new BusinessException(NULL_ERROR, "标签不能为空");
        }
        if (tags.length > 10) {
            throw new BusinessException(PARAMS_ERROR, "最多设置十个标签");
        }
        User user = new User();
        user.setId(currentUser.getId());
        // 将数组转成 json 字符串
        Gson gson = new Gson();
        String tagJsonStr = gson.toJson(tags);
        user.setTags(tagJsonStr);
        boolean res = updateById(user);
        if (res) {
            // 执行更新操作后更新 redis 中的缓存数据， 保证数据的一致性
            User newUserInfo = this.getById(user.getId());
            User safeUser = this.getSafeUser(newUserInfo);
            redisTemplate.opsForValue().set("user:login:" + user.getId(), safeUser, 30, TimeUnit.MINUTES);
            // 更新推荐用户列表 防止数据不统一
            Set<String> keys = redisTemplate.keys("user:recommend:" + "*");
            redisTemplate.delete(keys);
        }
        return false;
    }

    @Override
    public List<UserVO> getFans(HttpServletRequest request) {
        // 非空
        if (request == null) {
            throw new BusinessException(NULL_ERROR);
        }
        // 是否登录
        User currentUser = this.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        long id = currentUser.getId();
        List<User> fans = userFriendMapper.getFansByUserId(id);
        ArrayList<UserVO> fansList = new ArrayList<>();
        List<User> loves = userFriendMapper.getLovesByUserId(currentUser.getId());
        ArrayList<Long> loveIdList = new ArrayList<>();
        for (int i = 0; i < loves.size(); i++) {
            loveIdList.add(loves.get(i).getId());
        }
        // 用户信息脱敏
        for (User fan : fans) {
            UserVO userVO = new UserVO();
            User safeUser = this.getSafeUser(fan);
            BeanUtils.copyProperties(safeUser, userVO);
            // 是否关注
            if(loveIdList.contains(fan.getId())){
                userVO.setIsFollow(true);
            }else{
                userVO.setIsFollow(false);
            }
            fansList.add(userVO);
        }
        return fansList;
    }

    @Override
    public Boolean updatePassword(String newPassword, String email, String inputCode, String loginAccount, String checkPassword) {
        if (org.apache.commons.lang3.StringUtils.isAnyBlank(newPassword, email, inputCode, loginAccount, checkPassword)) {
            throw new BusinessException(NULL_ERROR, "请求参数为空");
        }
        String code = redisTemplate.opsForValue().get("sendEmail:" + email).toString();
        if (org.apache.commons.lang3.StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期");
        }
        if (!inputCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        User userInfo = this.lambdaQuery().eq(User::getLoginAccount, loginAccount).one();
        if(!email.equals(userInfo.getEmail())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已绑定其他邮箱");
        }
        // 密码加密
        String password = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        // 更新
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getLoginAccount, loginAccount);
        User user = new User();
        user.setLoginPassword(password);
        // 移除 redis 中的用户信息
        long userId = this.lambdaQuery().eq(User::getLoginAccount, loginAccount).one().getId();
        redisTemplate.delete("user:login:" + userId);
        return this.update(user, lambdaQueryWrapper);
    }


}


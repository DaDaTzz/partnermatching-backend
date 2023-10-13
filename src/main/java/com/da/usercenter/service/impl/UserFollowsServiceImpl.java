package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.model.entity.UserFollows;
import com.da.usercenter.service.UserFollowsService;
import com.da.usercenter.mapper.UserFollowsMapper;
import org.springframework.stereotype.Service;

/**
* @author 达
* @description 针对表【user_friend(好友关系)】的数据库操作Service实现
* @createDate 2023-09-29 01:05:03
*/
@Service
public class UserFollowsServiceImpl extends ServiceImpl<UserFollowsMapper, UserFollows>
    implements UserFollowsService {

}





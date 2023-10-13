package com.da.usercenter.mapper;

import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.entity.UserFollows;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author 达
* @description 针对表【user_friend(好友关系)】的数据库操作Mapper
* @createDate 2023-09-29 01:05:03
* @Entity com.da.usercenter.model.entity.UserFollows
*/
public interface UserFollowsMapper extends BaseMapper<UserFollows> {

    /**
     * 根据用户 id 获取好友信息
     * @param id
     * @return
     */
    List<User> getLovesByUserId(long id);
    List<User> getFansByUserId(long id);
}





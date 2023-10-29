package com.da.usercenter.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.da.usercenter.model.entity.Post;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 帖子数据库操作
 *
 * @author Da
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询帖子列表（包括已被删除的数据）
     */
    List<Post> listPostWithDelete(Date minUpdateTime);


    /**
     * 获取给我文章点赞的用户id集合
     * @param currentUserId
     * @return
     */
    @Select("select p.title '标题', p.content '内容', p.img, p.id '文章id', pt.user_id '点赞的用户id',pt.create_time '点赞时间' from post p join post_thumb pt on p.id = pt.post_id where p.user_id = #{currentUserId} and p.is_delete = 0 order by pt.create_time desc;" )
    List<Map<String,Object>> getLikeMyPostUserIdList(Long currentUserId);



}





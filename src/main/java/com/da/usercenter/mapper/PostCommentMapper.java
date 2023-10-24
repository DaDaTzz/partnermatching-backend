package com.da.usercenter.mapper;

import com.da.usercenter.model.entity.PostComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author 达
* @description 针对表【post_comment(帖子评论)】的数据库操作Mapper
* @createDate 2023-10-16 16:03:03
* @Entity com.da.usercenter.model.entity.PostComment
*/
public interface PostCommentMapper extends BaseMapper<PostComment> {

    /**
     * 获取给我点赞的用户 id 集合
     *
     * @param
     * @param currentUserId
     * @return
     */
    @Select("select pc.content '内容',pc.post_id '文章id', pc.user_id '创建评论的用户id', ct.comment_id '评论的id', ct.user_id '点赞的用户id',ct.create_time '点赞时间' from post_comment pc join comment_thumb ct on pc.id = ct.comment_id where pc.user_id = #{currentUserId} order by ct.create_time desc" )
    List<Map<String,Object>> getLikeMyCommentUserIdList(Long currentUserId);

}





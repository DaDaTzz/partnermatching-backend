package com.da.usercenter.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 聊天室消息
 * @TableName room_message
 */
@TableName(value ="room_message")
@Data
public class RoomMessage implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队伍id（公开房间的队伍 id 为 null）
     */
    private Long teamid;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户头像
     */
    private String userAwata;

    /**
     * 消息
     */
    private String message;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
package com.da.usercenter.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户消息
 * @TableName user_message
 */
@TableName(value ="user_message")
@Data
public class UserMessage implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送信息用户 id
     */
    private Long fromId;

    /**
     * 接受信息用户 id
     */
    private Long toId;

    /**
     * 发送信息用户昵称
     */
    private String fromNickname;

    /**
     * 接收信息用户昵称
     */
    private String toNickname;

    /**
     * 发送信息用户头像
     */
    private String fromAwata;

    /**
     * 接收信息用户头像
     */
    private String toAwata;

    /**
     * 信息
     */
    private String message;

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
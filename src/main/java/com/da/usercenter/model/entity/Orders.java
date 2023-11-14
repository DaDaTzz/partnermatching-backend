package com.da.usercenter.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 订单
 * @TableName orders
 */
@TableName(value ="orders")
@Data
public class Orders implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 收货地址
     */
    private String address;

    /**
     * 商品 id
     */
    private Long goodsId;

    /**
     * 下单数量
     */
    private Integer goodsNumber;

    /**
     * 0-未完成 1-已完成
     */
    private Integer states;

    /**
     * 金额
     */
    private Long amount;

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
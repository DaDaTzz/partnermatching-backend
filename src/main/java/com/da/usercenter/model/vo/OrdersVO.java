package com.da.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.da.usercenter.model.entity.Goods;
import lombok.Data;

import java.util.Date;

@Data
public class OrdersVO {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
     * 商品信息
     */
    private Goods goods;

    /**
     * 下单数量
     */
    private Integer goodsNumber;

    /**
     * 金额
     */
    private Long amount;

    /**
     * 0-未完成 1-已完成
     */
    private Integer states;

    /**
     * 创建时间
     */
    private Date createTime;

}

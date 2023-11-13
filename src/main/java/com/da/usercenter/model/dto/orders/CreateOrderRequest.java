package com.da.usercenter.model.dto.orders;

import lombok.Data;

@Data
public class CreateOrderRequest {
    /**
     * 商品 id
     */
    private Long goodsId;

    /**
     * 数量
     */
    private Integer goodsNumber;

    /**
     * 发货地址
     */
    private String address;

}

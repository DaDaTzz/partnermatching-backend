package com.da.usercenter.model.dto.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.da.usercenter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 3542949149379492880L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品价格（所需积分）
     */
    private Long price;


    /**
     * 商品简介
     */
    private String  description;
}

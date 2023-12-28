package com.da.usercenter.model.dto.trends;

import lombok.Data;

import java.io.Serializable;

/**
 * 动态点赞请求
 *
 * @author Da
 */
@Data
public class TrendsThumbAddRequest implements Serializable {

    private static final long serialVersionUID = 7708594775303240304L;
    /**
     * 动态 id
     */
    private Long trendsId;


}
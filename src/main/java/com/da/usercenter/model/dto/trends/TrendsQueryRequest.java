package com.da.usercenter.model.dto.trends;

import com.da.usercenter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author Da
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrendsQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = -5137792785762083614L;
    /**
     * id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;
    

    /**
     * 用户 id
     */
    private Long userId;

    

    
}
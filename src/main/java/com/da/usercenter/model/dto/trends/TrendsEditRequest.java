package com.da.usercenter.model.dto.trends;

import lombok.Data;

import java.io.Serializable;

@Data
public class TrendsEditRequest implements Serializable {
    private static final long serialVersionUID = 3950444552115725537L;
    /**
     * id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;


}

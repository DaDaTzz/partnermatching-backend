package com.da.usercenter.model.dto.team;

import lombok.Data;

/**
 * 加入队伍请求封装类
 */
@Data
public class TeamJoinRequest {

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 队伍密码
     */
    private String password;
}

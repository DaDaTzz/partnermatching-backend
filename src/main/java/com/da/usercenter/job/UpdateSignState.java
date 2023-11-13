package com.da.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 更新用户签到状态任务
 */
@Component
@Slf4j
public class UpdateSignState {

    @Resource
    private UserService userService;


    /**
     * 更新每日签到状态
     */
    @Scheduled(cron = "0 0 0 ? * *") // 每天0:00执行
    public void doUpdateSignState(){
        User user = new User();
        user.setSign(0);
        userService.update(user, new LambdaQueryWrapper<>());
        log.info("已重置用户签到状态");
    }
}

package com.da.usercenter.manager;

import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 提供 RedisLimiter 限流基础服务
 */
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作
     *
     * @param key 区分不同的限流器，比如不同的用户 id 应该分别统计
     */
    public void doRateLimit(String key) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 1分钟一次
        rateLimiter.trySetRate(RateType.OVERALL, 1, 1, RateIntervalUnit.MINUTES);
        // 每当一个操作来了后，请求一个令牌
        boolean conOp = rateLimiter.tryAcquire(1);
        if (!conOp) {
            throw new BusinessException(ErrorCode.NO_AUTH, "请求过于频繁");
        }

    }
}

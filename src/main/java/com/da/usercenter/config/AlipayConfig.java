package com.da.usercenter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * 阿里沙箱环境配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /**
     * appId
     */
    private String appId;

    /**
     * 私钥
     */
    private String appPrivateKey;

    /**
     * 公钥
     */
    private String alipayPublicKey;

    /**
     * 回调地址
     */
    private String notifyUrl;

}

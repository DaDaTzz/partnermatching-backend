package com.da.usercenter.controller;

import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.config.AlipayConfig;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/alipay")
public class AlipayController {
    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYP = "RSA2";

    @Resource
    private AlipayConfig alipayConfig;

    @Resource
    private UserService userService;

    @GetMapping("/pay")
    public void pay(Long payIntegral, HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (payIntegral == null || payIntegral <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DefaultAlipayClient defaultAlipayClient = new DefaultAlipayClient(GATEWAY_URL, alipayConfig.getAppId(), alipayConfig.getAppPrivateKey(), FORMAT, CHARSET, alipayConfig.getAlipayPublicKey(), SIGN_TYP);


        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();
        alipayTradePagePayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());
        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", UUID.randomUUID());
        bizContent.set("total_amount", payIntegral);
        bizContent.set("subject", "积分订单");
        bizContent.set("product_code", "FAST_INSERT_TRADE_PAY");
        alipayTradePagePayRequest.setBizContent(bizContent.toString());
        alipayTradePagePayRequest.setReturnUrl("http://www.iyaya/user/shoppingMall");

        String form = "";
        try {
            form = defaultAlipayClient.pageExecute(alipayTradePagePayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=" + CHARSET);
        response.getWriter().write(form);
        response.getWriter().flush();
        response.getWriter().close();

    }

    @PostMapping("/notify")  // 注意这里必须是POST接口
    public void payNotify(HttpServletRequest request) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            //System.out.println("=========支付宝异步回调========");

            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayConfig.getAlipayPublicKey(), "UTF-8");
            if (checkSignature) {
                User currentUser = userService.getCurrentUser(request);
                if (currentUser == null) {
                    throw new BusinessException(ErrorCode.NOT_LOGIN);
                }
                currentUser.setIntegral(Long.valueOf(currentUser.getIntegral() + params.get("total_amount")));
                userService.updateById(currentUser);
            }
        }

    }

}

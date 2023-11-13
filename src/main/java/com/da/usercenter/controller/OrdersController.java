package com.da.usercenter.controller;

import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.exception.ThrowUtils;
import com.da.usercenter.model.dto.orders.CreateOrderRequest;
import com.da.usercenter.model.entity.Goods;
import com.da.usercenter.model.entity.Orders;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.GoodsService;
import com.da.usercenter.service.OrdersService;
import com.da.usercenter.service.UserService;
import javafx.scene.layout.BorderStrokeStyle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/orders")
public class OrdersController {
    @Resource
    private UserService userService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private OrdersService ordersService;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 创建订单
     * @param request
     * @return
     */
    @PostMapping("/create")
    @Transactional
    public ResponseResult<Boolean> createOrder(@RequestBody CreateOrderRequest createOrderRequest, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(currentUser == null, new BusinessException(ErrorCode.NOT_LOGIN));
        Long goodsId = createOrderRequest.getGoodsId();
        String address = createOrderRequest.getAddress();
        Integer goodsNumber = createOrderRequest.getGoodsNumber();
        ThrowUtils.throwIf(StringUtils.isBlank(address) || goodsId <= 0 || goodsId == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        // 下单数不能超过 10
        ThrowUtils.throwIf(goodsNumber > 10, new BusinessException(ErrorCode.PARAMS_ERROR, "下单数不能超过10件"));
        // 商品是否存在
        Goods goods = goodsService.getById(goodsId);
        ThrowUtils.throwIf(goods == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        // 库存是否充足
        ThrowUtils.throwIf(goodsNumber > goods.getNumber(), new BusinessException(ErrorCode.PARAMS_ERROR, "库存不足"));
        // 用户积分是否充足
        Long price = goods.getPrice() * goodsNumber;
        ThrowUtils.throwIf(currentUser.getIntegral() < price, new BusinessException(ErrorCode.PARAMS_ERROR, "积分不足，请充值！"));
        // 下订单
        // 1、扣除用户积分
        User user = new User();
        user.setId(currentUser.getId());
        user.setIntegral(currentUser.getIntegral() - price);
        userService.updateById(user);
        // 更新缓存用户信息
        User u = userService.getById(currentUser.getId());
        User safeUser = userService.getSafeUser(u);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("user:login:" + safeUser.getId(), safeUser, 30, TimeUnit.MINUTES);
        // 2、减去商品库存
        goods.setNumber(goods.getNumber() - goodsNumber);
        goodsService.updateById(goods);
        // 3、创建订单
        Orders orders = new Orders();
        orders.setUserId(currentUser.getId());
        orders.setAddress(address);
        orders.setGoodsId(goodsId);
        orders.setGoodsNumber(goodsNumber);
        // TODO: 2023/11/14 优化：使用消息队列
        return ResponseResult.success(ordersService.save(orders));
    }
}

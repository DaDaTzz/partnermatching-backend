package com.da.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.exception.ThrowUtils;
import com.da.usercenter.model.dto.orders.CreateOrderRequest;
import com.da.usercenter.model.dto.orders.GetOrdersByIdRequest;
import com.da.usercenter.model.dto.orders.RefundRequest;
import com.da.usercenter.model.entity.Goods;
import com.da.usercenter.model.entity.Orders;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.vo.OrdersVO;
import com.da.usercenter.service.GoodsService;
import com.da.usercenter.service.OrdersService;
import com.da.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.da.usercenter.constant.UserConstant.ADMIN_USER;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/orders")
@Slf4j
public class OrdersController {
    @Resource
    private UserService userService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private OrdersService ordersService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 创建订单
     *
     * @param request
     * @return
     */
    @PostMapping("/create")
    @Transactional
    public ResponseResult<Boolean> createOrder(@RequestBody CreateOrderRequest createOrderRequest, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(currentUser == null, new BusinessException(ErrorCode.NOT_LOGIN));
        Long goodsId = createOrderRequest.getGoodsId();
        String address = createOrderRequest.getAddress();
        Integer goodsNumber = createOrderRequest.getGoodsNumber();
        ThrowUtils.throwIf(StringUtils.isBlank(address) || goodsId <= 0 || goodsId == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        RLock lock = redissonClient.getLock("create_orders");
        // 只有一个线程能获取到锁
        try {
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    // TODO: 2023/11/14 优化：使用消息队列
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
                    orders.setAmount(price);
                    orders.setGoodsNumber(goodsNumber);
                    return ResponseResult.success(ordersService.save(orders));
                }
            }
        } catch (Exception e) {
            log.error("create orders err");
            return ResponseResult.success(false);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    /**
     * 我的订单
     *
     * @param request
     * @return
     */
    @GetMapping("/my")
    public ResponseResult<List<OrdersVO>> getMyOrders(Integer states, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(currentUser == null, new BusinessException(ErrorCode.NOT_LOGIN));
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getUserId, currentUser.getId());
        ordersLambdaQueryWrapper.eq(states != null && states >= 0, Orders::getStates, states);
        ordersLambdaQueryWrapper.orderByDesc(Orders::getCreateTime);
        List<Orders> ordersList = ordersService.list(ordersLambdaQueryWrapper);
        if (ordersList.size() == 0) {
            return ResponseResult.success(new ArrayList<>());
        }
        ArrayList<OrdersVO> ordersVOList = new ArrayList<>();
        for (Orders orders : ordersList) {
            OrdersVO ordersVO = new OrdersVO();
            BeanUtils.copyProperties(orders, ordersVO);
            Goods goods = goodsService.getById(orders.getGoodsId());
            ordersVO.setGoods(goods);
            ordersVOList.add(ordersVO);
        }
        return ResponseResult.success(ordersVOList);
    }

    /**
     * 退款
     * @param refundRequest
     * @param request
     * @return
     */
    @PostMapping("/refund")
    @Transactional
    public  ResponseResult<Boolean> refund(@RequestBody RefundRequest refundRequest, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long orderId = refundRequest.getOrderId();
        if(orderId == null || orderId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 订单是否存在
        Orders orders = ordersService.getById(orderId);
        if(orders == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 是否为当前用户的订单（只有本人和管理员可以退款）
        if(currentUser.getType() != ADMIN_USER && orders.getUserId() != currentUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 下单时间超过七天不允许退款
        long orderCreateTime = orders.getCreateTime().toInstant().toEpochMilli();
        long now = new Date().toInstant().toEpochMilli();
        long diff = now - orderCreateTime;
        long days = diff / (24 * 60 * 60 * 1000);
        if(days > 7){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "七天后不允许退款");
        }
        // 已发货的订单不允许退款
        if(orders.getStates() == 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已发货，无法退款！");
        }
        // 退款
        // 1、删除订单信息
        boolean res1 = ordersService.removeById(orders.getId());
        // 2、库存返回
        Goods goods = goodsService.getById(orders.getGoodsId());
        Goods newGoods = new Goods();
        newGoods.setId(goods.getId());
        newGoods.setNumber(goods.getNumber() + orders.getGoodsNumber());
        boolean res2 = goodsService.updateById(newGoods);
        // 3、返还积分
        User user = userService.getById(currentUser.getId());
        User newUser = new User();
        newUser.setId(user.getId());
        newUser.setIntegral(user.getIntegral() + orders.getAmount());
        boolean res3 = userService.updateById(newUser);
        // 4、更新缓存用户信息
        User u = userService.getById(currentUser.getId());
        User safeUser = userService.getSafeUser(u);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("user:login:" + safeUser.getId(), safeUser, 30, TimeUnit.MINUTES);
        if(res1 && res2 && res3){
            return ResponseResult.success(true);
        }
        return ResponseResult.success(false);
    }


    /**
     * 根据 id 获取订单信息
     * @param getOrdersByIdRequest
     * @param request
     * @return
     */
    @PostMapping("/get/vo")
    public ResponseResult<OrdersVO> getOrdersById(@RequestBody GetOrdersByIdRequest getOrdersByIdRequest, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long orderId = getOrdersByIdRequest.getOrderId();
        Orders orders = ordersService.getById(orderId);
        if(orders == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OrdersVO ordersVO = new OrdersVO();
        BeanUtils.copyProperties(orders, ordersVO);
        Goods goods = goodsService.getById(orders.getGoodsId());
        ordersVO.setGoods(goods);
        return ResponseResult.success(ordersVO);
    }

}

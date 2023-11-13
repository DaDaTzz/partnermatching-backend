package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.model.entity.Orders;
import com.da.usercenter.service.OrdersService;
import com.da.usercenter.mapper.OrdersMapper;
import org.springframework.stereotype.Service;

/**
* @author 达
* @description 针对表【orders(订单)】的数据库操作Service实现
* @createDate 2023-11-13 22:37:25
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{

}





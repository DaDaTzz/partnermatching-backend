package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.model.entity.Trends;
import com.da.usercenter.service.TrendsService;
import com.da.usercenter.mapper.TrendsMapper;
import org.springframework.stereotype.Service;

/**
* @author 达
* @description 针对表【trends(朋友圈)】的数据库操作Service实现
* @createDate 2023-12-25 12:09:29
*/
@Service
public class TrendsServiceImpl extends ServiceImpl<TrendsMapper, Trends>
    implements TrendsService{

}





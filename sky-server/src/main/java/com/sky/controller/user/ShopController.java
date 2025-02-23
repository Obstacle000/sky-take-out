package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
//不起别名的话编译时会报错
@RestController("userShopController")
@RequestMapping("/user/shop")
@Api("店铺接口")
@Slf4j
public class ShopController {
    public static final String KEY = "SHOP_STATUS";

    @Autowired
    RedisTemplate redisTemplate;


    @GetMapping("/status")
    @ApiOperation("用户端获取店铺状态")
    public Result<Integer> getStatus(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer shopStatus = (Integer) valueOperations.get(KEY);
        return Result.success(shopStatus);
    }
}

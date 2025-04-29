package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品: {}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        String key="dish_"+dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /**
     * 菜品分页
     * @param queryDTO
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO queryDTO) {
        log.info("菜品分页查询: {}", queryDTO);
        PageResult pageResult = dishService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品删除
     * @param ids
     * @return
     */
    @ApiOperation("菜品删除")
    @DeleteMapping
    //前端请求的数据是字符串类型,而我们要求MVC自动解析字符串,用逗号分割装到list里面, 用注解实现
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品删除id: {}", ids);
        dishService.deleteBatch(ids);
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 修改时回显
     * @param id
     * @return
     */
    @ApiOperation("修改时回显")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id: {}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 菜品修改
     * @param dishDTO
     * @return
     */
    @ApiOperation("修改菜品")
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品: {}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        cleanCache("dish_*");
        return Result.success();
    }
    @ApiOperation("菜品起售,停售")
    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status,Integer id) {
        log.info("菜品id :{}",id);
        dishService.startOrStop(status,id);
        cleanCache("dish_*");
        return Result.success();
    }
    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }
    //清理缓存数据
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);

    }



}

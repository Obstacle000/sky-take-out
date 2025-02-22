package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {
    /**
     * 根据dishiid删除口味数据
     *
     * @param ids
     */
    void deleteByDishId(List<Long> ids);

    /**
     * 重载方法
     * @param dish
     */
    //不知道为什么我一开始用了方法重载方法名和上面一样,但是idea识别不了下面的方法
    @Delete("DELETE FROM dish_flavor WHERE dish_id = #{id}")
    void deleteByDishIdWithDish(Dish dish);

    /**
     * 根据dishId获取口味数据
     *
     * @param dishId
     * @return
     */
    @Select("SELECT * FROM dish_flavor WHERE dish_id = #{dishId}")
    List<DishFlavor> selectByDishId(Long dishId);

}
package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishFlavorMapper,DishFlavor> implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //插入一条菜品数据到菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insert(dish);
        //插入多条口味数据到口味表
        //我们知道口味表关联了菜品表的id,所以我们还需要获取刚才插入的菜品的id
        Long id = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //遍历集合填入菜品id
            flavors.forEach(flavor -> {flavor.setDishId(id);});

            //使用IService的方法批量插入,其余字段
            saveBatch(flavors);

        }

    }
    /**
     * 菜品分页
     * @param queryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO queryDTO) {
        //mybatis版本的分页查询
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(queryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }
    /**
     * 菜品删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断是否能删除-查看状态
        for (Long id : ids) {
            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus()== StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //是否关联了套餐
        //TODO 这里其实还没有让菜品关联套餐,所以肯定是能删的
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品数据
        dishMapper.deleteBatchIds(ids);
        //删除口味
        dishFlavorMapper.deleteByDishId(ids);


    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //之前使用了外连接进行封装,这次我们试一下分两步封装
        Dish dish = dishMapper.selectById(id);
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectByDishId(id);
        //拼起来
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 修改菜品和口味
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改基本信息
        dishMapper.updateById(dish);
        //删除原有的口味数据,在插入新来的口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectByDishId(dish.getId());
        //防止一开始添加的时候就没有口味
        if (dishFlavors != null && dishFlavors.size() > 0) {
            dishFlavorMapper.deleteByDishIdWithDish(dish);
        }
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //遍历集合填入菜品id
            flavors.forEach(flavor -> {flavor.setDishId(dish.getId());});

            //使用IService的方法批量插入,其余字段
            saveBatch(flavors);

        }

    }
    /**
     * 起售停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Integer id){
        dishMapper.updateStatus(status,id);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}

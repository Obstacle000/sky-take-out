package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类业务层
 */
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        //属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);

        //分类状态默认为禁用状态0
        category.setStatus(StatusConstant.ENABLE);

        //设置创建时间、修改时间、创建人、修改人
        //不用写了,有全局自动填充
        /*category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());*/

        categoryMapper.insert(category);
    }

    /**
     * 分页查询
     * @param queryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO queryDTO) {
        // 构造分页对象，传入当前页和每页记录数
        Page<Category> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 构造查询条件，使用 LambdaQueryWrapper 避免硬编码字段名
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        // 如果传入了 name，进行模糊搜索
        queryWrapper.like(queryDTO.getName() != null && !queryDTO.getName().isEmpty(), Category::getName, queryDTO.getName());

        // 如果传入了 type，添加 type 的过滤条件

        queryWrapper.eq(queryDTO.getType() != null,Category::getType, queryDTO.getType());


        // 状态为 1
        queryWrapper.eq(Category::getStatus, 1);

        // 排序 by sort 升序 和 create_time 降序
        queryWrapper.orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime);

        // 执行分页查询
        IPage<Category> categoryPage = categoryMapper.selectPage(page, queryWrapper);

        // 封装分页结果，并返回
        return new PageResult(categoryPage.getTotal(), categoryPage.getRecords());
    }




    /**
     * 根据id删除分类
     * @param id
     */
    public void deleteById(Long id) {
        //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        Integer count = dishMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = setmealMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //删除分类数据
        categoryMapper.deleteById(id);
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);

        //设置修改时间、修改人
        //category.setUpdateTime(LocalDateTime.now());
        //category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.updateById(category);
    }

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {

        Category category = Category.builder()
                .id(id)
                .status(status)
                //.updateTime(LocalDateTime.now())
                //.updateUser(BaseContext.getCurrentId())
                .build();
        LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<>();
        lqw.eq(Category::getId,id);

        categoryMapper.update(category,lqw);
    }

    /**
     * 只分类查询
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<>();
        lqw.eq(Category::getType,type);
        return categoryMapper.selectList(lqw);
    }


}

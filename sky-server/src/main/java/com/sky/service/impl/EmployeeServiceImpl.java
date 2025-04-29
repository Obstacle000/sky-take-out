package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.BCryptUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        password=DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //对象属性拷贝,前面是要拷贝的,后面的是目标
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setStatus(StatusConstant.ENABLE);

        //TODO 设置密码,默认123456,以后添加修改密码业务
        String pwd = DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes());
        employee.setPassword(pwd);

        //设置记录时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
        //由于没办法获取,暂时先写一个固定的值,后面会学新技术
        Long currentId = BaseContext.getCurrentId();
        employee.setCreateUser(currentId);
        employee.setUpdateUser(currentId);

        //插入用户
        employeeMapper.insert(employee);

    }
    /**
     * 分页操作
     * @param queryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO queryDTO) {
        // 构造分页对象
        Page<Employee> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 构造查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(queryDTO.getName() != null && !queryDTO.getName().isEmpty(), Employee::getName, queryDTO.getName())
                .orderByDesc(Employee::getCreateTime);

        // 执行分页查询
        IPage<Employee> employeePage = employeeMapper.selectPage(page, queryWrapper);

        // 封装结果并返回
        return new PageResult(employeePage.getTotal(), employeePage.getRecords());
    }

    /**
     * 修改员工状态
     * @param status
     * @param id
     */
    public void startOrstop(Integer status, Long id) {
        //build风格创建对象
        Employee employee=Employee.builder()
                .status(status)
                .id(id)
                .build();

        // employee.setUpdateTime(LocalDateTime.now());
        Long currentId = BaseContext.getCurrentId();
        employee.setUpdateUser(currentId);

        LambdaQueryWrapper<Employee> lqw=new LambdaQueryWrapper<Employee>();
        lqw.eq(Employee::getId,id);

        employeeMapper.update(employee,lqw);
    }

    /**
     * 根据id查询对象
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.selectById(id);

        //密码不准看
        employee.setPassword("****");
        return employee;
    }
    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {



        //注意1:由于BaseMapper的泛型是employee,所以得进行转换,用copy
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        //注意2:LambdaQueryWrapper的泛型不能写DTO了,不然最后update传递不了lqw
        LambdaQueryWrapper<Employee> lqw=new LambdaQueryWrapper<>();
        lqw.eq(Employee::getId,employee.getId());

        employeeMapper.update(employee,lqw);

    }

}

package com.sky.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @ApiOperation(value = "员工登录")
    @PostMapping("/login")

    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌,这里相当于cookie的操作,不是加密
        Map<String, Object> claims = new HashMap<>();
        //这里之所以以id作为token,是因为后面还要从token里面用ThreadLocal获取id
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);
        //这个builer是另一种创建对象的方式,前提是有@builder注解(lombok)
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     *员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @ApiOperation("员工分页查询")
    @GetMapping("/page")
    //对于查询的接口,建议写泛型
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询,参数为: {}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     *
     * @param employeeDTO
     * @return
     */
    @ApiOperation(value="新增员工")
    @PostMapping()
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工: {}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 启用禁用员工
     * @param status
     * @param id
     * @return
     */
    @ApiOperation(value = "修改员工状态")
    @PostMapping("/status/{status}")
    public Result startOrstop(@PathVariable Integer status,Long id ) {
        log.info("启用禁用员工账号: {},{}",status,id);
        employeeService.startOrstop(status,id);
        return Result.success();
    }

    /**
     * 修改员工时回显
     * @param id
     * @return
     */
    @ApiOperation(value = "修改员工时回显")
    @GetMapping("/{id}")
    public Result<Employee> getById( @PathVariable Long id) {
        Employee employee = employeeService.getById(id);

        return Result.success(employee);
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    @ApiOperation("编辑员工信息")
    @PutMapping//更新用put.新增时post
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工信息");
        employeeService.update(employeeDTO);
        return Result.success();
    }






























    /**
     * 退出
     *
     * @return
     */
    @ApiOperation(value = "员工登出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }
}

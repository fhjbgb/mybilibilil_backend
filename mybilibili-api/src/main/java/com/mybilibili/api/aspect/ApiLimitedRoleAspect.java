package com.mybilibili.api.aspect;


import com.mybilibili.api.support.UserSupport;
import com.mybilibili.domain.annotation.ApiLimitedRole;
import com.mybilibili.domain.auth.UserRole;
import com.mybilibili.domain.exception.ConditionException;
import com.mybilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//Order :说明优先级
@Order(1)
//标记当前类为组件
@Component
//切面类
@Aspect
public class ApiLimitedRoleAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    //切点标识，何时切入 在执行ApiLimitedRole注解时切入，就执行check方法
    @Pointcut("@annotation(com.mybilibili.domain.annotation.ApiLimitedRole)")
    //切点方法
    public void check(){

    }
    //参数：作用在哪些方法上
    //在check之前并且有ApiLimitedRole注解的实例。下面的apiLimitedRole就是参数的apiLimitedRole，可以自行改名
    @Before("check() && @annotation(apiLimitedRole)")
    //参数：
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole){
        Long userId = userSupport.getCurrentUserId();
        //用户关联的角色
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        //限制权限的列表信息
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
        //权限限制的列表
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        //角色权限关联表
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        //两set取交集
        roleCodeSet.retainAll(limitedRoleCodeSet);
        if(roleCodeSet.size() > 0){
            throw new ConditionException("权限不足！");
        }
    }

}

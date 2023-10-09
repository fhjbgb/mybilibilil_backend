package com.mybilibili.api.aspect;


import com.mybilibili.api.support.UserSupport;
import com.mybilibili.domain.UserMoment;
import com.mybilibili.domain.annotation.ApiLimitedRole;
import com.mybilibili.domain.auth.UserRole;
import com.mybilibili.domain.constant.AuthRoleConstant;
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
@Component
@Aspect
public class DataLimitedAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    //切点
    @Pointcut("@annotation(com.mybilibili.domain.annotation.DataLimited)")
    public void check(){

    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint){
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        //获取所有参数
        Object[] args = joinPoint.getArgs();
        for(Object arg : args){
            //遍历到userMoment类型
            if(arg instanceof UserMoment){
                UserMoment userMoment = (UserMoment)arg;
                String type = userMoment.getType();
                //等级判断
                if(roleCodeSet.contains(AuthRoleConstant.ROLE_LV1) && !"0".equals(type)){
                    throw new ConditionException("参数异常");
                }
            }
        }
    }

}

package com.mybilibili.domain.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

//Annotation:注解类
//定义该注解辅助aop的实现
//在运行时执行
@Retention(RetentionPolicy.RUNTIME)
//目标：放在方法中
@Target({ElementType.METHOD})
@Documented
@Component
public @interface ApiLimitedRole {

    String[] limitedRoleCodeList() default {};
}

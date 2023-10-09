package com.mybilibili.domain.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

//Annotation:注解类

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Component
public @interface DataLimited {


}

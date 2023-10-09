package com.mybilibili.service.handler;


import com.mybilibili.domain.JsonResponse;
import com.mybilibili.domain.exception.ConditionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

//全局异常处理
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {
    //标记异常处理器
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    //参数:请求数据，错误
    public JsonResponse<String> commonExceprionHandler(HttpServletRequest request, Exception e){
        //获取错误信息
        String errorMsg = e.getMessage();
        //判断错误类型
        if(e instanceof ConditionException){
            String errorCode = ((ConditionException)e).getCode();
            return new JsonResponse<>(errorCode, errorMsg);
        }
        else {
            return new JsonResponse<>("500",errorMsg);
        }
    }
}

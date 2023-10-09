package com.mybilibili.domain.exception;

//异常定义相关实体类  条件异常
public class ConditionException extends RuntimeException{

    private static final long serivalVersionUID =1L;
    //状态码
    private String code;

    //初始化

    public ConditionException(String code,String name){
        super(name);
        this.code = code;
    }

    public ConditionException(String name){
        super(name);
        this.code = "500";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

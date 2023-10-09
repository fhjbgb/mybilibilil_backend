package com.mybilibili.domain;

//json格式的返回结果
public class JsonResponse <T>{
    //状态码
    private String code;
    //提示语
    private String msg;
    //返回数据类型
    private T data;

    //构造方法
    //失败的方法，要将失败的提示信息和状态码传入
    public JsonResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    //成功的方法，直接传入数据类型即可
    public JsonResponse(T data) {
        this.data = data;
        this.msg = "成功";
        this.code = "0";
    }

    public static JsonResponse<String> success(){
        return new JsonResponse<>(null);
    }

    public static JsonResponse<String> success(String data){
        return new JsonResponse<>(data);
    }

    public static JsonResponse<String> fail(){
        return new JsonResponse<>("1", "失败");
    }

    public static JsonResponse<String> fail(String code, String msg){
        return new JsonResponse<>(code, msg);
    }



    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

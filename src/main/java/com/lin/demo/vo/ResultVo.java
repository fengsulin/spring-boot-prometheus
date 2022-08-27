package com.lin.demo.vo;


import java.io.Serializable;

public class ResultVo<T> implements Serializable {
    private static final long serialVersionUID = 2L;

    /**状态码*/
    private Integer code;
    /**响应信息*/
    private String msg;
    /**响应数据*/
    private T data;

    public ResultVo(){}

    public ResultVo(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    /**只返回成功状态码*/
    public static ResultVo success(){
        return new ResultVo<>(ResultCode.SUCCESS.getCode(),ResultCode.SUCCESS.getMsg(),null);
    }
    public static <T> ResultVo success(String msg,T data){
        return new ResultVo<>(ResultCode.SUCCESS.getCode(),msg,data);
    }
    /**返回成功的数据对象*/
    public static <T> ResultVo success(T data){
        return new ResultVo<>(ResultCode.SUCCESS.getCode(),ResultCode.SUCCESS.getMsg(), data);
    }
    /**只返回请求失败的状态码*/
    public static ResultVo failed(){
        return new ResultVo<>(ResultCode.FAILED.getCode(),ResultCode.FAILED.getMsg(),null);
    }

    /**返回请求失败的错误信息*/
    public static ResultVo failed(String msg){
        return new ResultVo<>(ResultCode.FAILED.getCode(),msg,null);
    }

    /**返回指定的错误码和数据对象*/
    public static <T> ResultVo failed(ResultCode resultCode,T data){
        return new ResultVo<>(resultCode.getCode(),resultCode.getMsg(),data);
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
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

    @Override
    public String toString() {
        return "ResultVo{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}

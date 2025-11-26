package com.fengqi.fund.fundadvisor.dto;

import lombok.Data;

@Data
public class ResultDTO<T> {
    // 响应码（200成功，其他失败）
    private Integer code;
    // 响应消息
    private String message;
    // 响应数据
    private T data;

    // 成功响应（带数据）
    public static <T> ResultDTO<T> success(T data) {
        ResultDTO<T> result = new ResultDTO<>();
        result.setCode(200);
        result.setMessage("成功");
        result.setData(data);
        return result;
    }

    // 成功响应（带数据和消息）
    public static <T> ResultDTO<T> success(T data, String message) {
        ResultDTO<T> result = new ResultDTO<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 成功响应（无数据）
    public static ResultDTO<Void> success() {
        return success(null);
    }

    // 失败响应
    public static <T> ResultDTO<T> fail(Integer code, String message) {
        ResultDTO<T> result = new ResultDTO<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    // 错误响应（便捷方法）
    public static <T> ResultDTO<T> error(String message) {
        return fail(500, message);
    }
    
    // 判断是否成功
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}
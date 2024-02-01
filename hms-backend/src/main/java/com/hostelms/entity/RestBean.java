package com.hostelms.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

/**
 * RestBean 是一个通用的 RESTful 接口响应封装类，用于返回标准的接口响应结果。
 *
 * @param <T> 响应数据的类型
 */
public record RestBean<T>(int code, T data, String msg) {

    /**
     * 创建一个成功的响应对象，并指定响应数据。
     *
     * @param data 响应数据
     * @param <T>  响应数据的类型
     * @return 成功的响应对象
     */
    public static <T> RestBean<T> success(T data){
        return  new RestBean<>(200, data, "请求成功");
    }

    /**
     * 创建一个成功的响应对象，不包含响应数据。
     *
     * @param <T> 响应数据的类型
     * @return 成功的响应对象
     */
    public static <T> RestBean<T> success(){
        return success(null);
    }

    /**
     * 创建一个未授权的响应对象，并指定错误消息。
     *
     * @param msg 错误消息
     * @param <T> 响应数据的类型
     * @return 未授权的响应对象
     */
    public static <T> RestBean<T> unauthorized(String msg){
        return failure(401, msg);
    }

    /**
     * 创建一个禁止访问的响应对象，并指定错误消息。
     *
     * @param msg 错误消息
     * @param <T> 响应数据的类型
     * @return 禁止访问的响应对象
     */
    public static <T> RestBean<T> forbidden(String msg){
        return failure(403, msg);
    }

    /**
     * 创建一个失败的响应对象，并指定错误代码和错误消息。
     *
     * @param code 错误代码
     * @param msg  错误消息
     * @param <T>  响应数据的类型
     * @return 失败的响应对象
     */
    public static <T> RestBean<T> failure(int code,String msg){
        return new RestBean<>(code, null, msg);
    }

    /**
     * 将当前响应对象转换为 JSON 字符串。
     *
     * @return JSON 字符串表示的响应对象
     */
    public String asJsonString(){
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}

package com.example.aicourse.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应格式
 *
 * @param <T> 返回数据的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    /** 业务代码：0 表示成功，其它值表示各类错误 */
    private int code;
    /** 提示信息 */
    private String msg;
    /** 业务数据 */
    private T data;

    /** 成功但无返回数据 */
    public static <T> Result<T> ok() {
        return new Result<>(0, "success", null);
    }

    /** 成功且有返回数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }

    /** 失败：自定义消息 */
    public static <T> Result<T> error(String msg) {
        return new Result<>(1, msg, null);
    }
}
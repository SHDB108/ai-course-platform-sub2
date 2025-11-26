package com.example.aicourse.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 JSR-303 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public Result<String> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败: {}", errorMessage);
        return Result.error(errorMessage);
    }

    /**
     * 处理数据库唯一约束异常 (如用户名、学号重复)
     * 审阅建议返回 422，这里简化为 400 业务错误
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public Result<String> handleSqlIntegrityException(SQLIntegrityConstraintViolationException ex) {
        log.warn("数据库唯一约束异常: {}", ex.getMessage());
        if (ex.getMessage().contains("Duplicate entry")) {
            return Result.error("提交的数据与现有记录重复，请检查。");
        }
        return Result.error("数据库操作失败。");
    }

    /**
     * 处理其它所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public Result<String> handleAllExceptions(Exception ex) {
        log.error("服务器内部错误", ex);
        return Result.error("服务器内部错误，请联系管理员。");
    }
}
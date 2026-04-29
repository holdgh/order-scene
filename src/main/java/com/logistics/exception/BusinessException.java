package com.logistics.exception;

/**
 * @description 自定义业务异常
 * @author gaohuan
 * @create 2023-03-29 15:07
 **/
public class BusinessException extends RuntimeException {

    private final String message;

    public BusinessException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

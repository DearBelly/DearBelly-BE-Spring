package com.hanium.mom4u.global.exception;

import com.hanium.mom4u.global.response.StatusCode;

public class BusinessException extends RuntimeException {

    private final StatusCode statusCode;

    public BusinessException(StatusCode statusCode) {
        super(statusCode.getDescription());
        this.statusCode = statusCode;
    }

    public static BusinessException of(StatusCode statusCode) {
        return new BusinessException(statusCode);
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}

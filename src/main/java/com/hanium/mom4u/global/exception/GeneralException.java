package com.hanium.mom4u.global.exception;

import com.hanium.mom4u.global.response.StatusCode;

public class GeneralException extends RuntimeException {

    private final StatusCode statusCode;

    public GeneralException(StatusCode statusCode) {
        super(statusCode.getDescription());
        this.statusCode = statusCode;
    }

    public static GeneralException of(StatusCode statusCode) {
        return new GeneralException(statusCode);
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}

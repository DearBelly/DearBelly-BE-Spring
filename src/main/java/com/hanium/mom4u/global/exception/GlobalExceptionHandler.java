package com.hanium.mom4u.global.exception;

import com.hanium.mom4u.global.response.CommonResponse;
import com.hanium.mom4u.global.response.ErrorResponse;
import com.hanium.mom4u.global.response.StatusCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // GeneralException(RuntimeException)
    @ExceptionHandler(GeneralException.class)
    protected ResponseEntity<ErrorResponse> generalException(GeneralException e, HttpServletRequest request) {
        logError(e, request);
        return ErrorResponse.toResponseEntity(e.getStatusCode());
    }

    // BussinessException(성공 응답(200)이되, 에러 메세지를 담아서 반환)
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<CommonResponse> businessException(BusinessException e, HttpServletRequest request) {
        logError(e, request);
        return ResponseEntity.ok(CommonResponse.withMessage(e.getStatusCode()));
    }

    // Header missing Exception
    @ExceptionHandler(MissingRequestHeaderException.class)
    protected ResponseEntity<ErrorResponse> missingRequestHeaderException(MissingRequestHeaderException e, HttpServletRequest request) {
        logError(e, request);
        return ErrorResponse.toResponseEntity(StatusCode.TOKEN_NOT_FOUND);
    }

    // 기타 예외들( NullPointerException, IllegalArgumentException 등 )
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        logError(e, request);
        return ErrorResponse.toResponseEntity(StatusCode.INTERNAL_SERVER_ERROR);
    }

    // console log 출력
    private void logError(Exception e, HttpServletRequest request) {
        log.error("Request URI : [{}] {}", request.getMethod(), request.getRequestURI());
        log.error("Exception : ", e);
        log.error(e.getMessage());
    }
}

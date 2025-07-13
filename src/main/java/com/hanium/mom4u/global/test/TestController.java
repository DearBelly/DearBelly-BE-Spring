package com.hanium.mom4u.global.test;

import com.hanium.mom4u.global.exception.BusinessException;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.CommonResponse;
import com.hanium.mom4u.global.response.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @GetMapping("/success/data")
    public ResponseEntity<CommonResponse<?>> successTest() {
        return ResponseEntity.ok(
                CommonResponse.onSuccess("success")
        );
    }

    @GetMapping("/success/null")
    public ResponseEntity<CommonResponse<?>> successNullTest() {
        return ResponseEntity.ok(
                CommonResponse.onSuccess()
        );
    }

    @GetMapping("/error")
    public ResponseEntity<CommonResponse<?>> errorTest() {
        throw BusinessException.of(StatusCode.FAILURE_TEST);
    }

    @GetMapping("/error/exception")
    public ResponseEntity<CommonResponse<?>> errorExceptionTest(
            @RequestParam String data
    ) throws GeneralException {
        if (data.isEmpty()) {
            throw GeneralException.of(StatusCode.FAILURE_TEST);
        }
        return ResponseEntity.ok(
                CommonResponse.onSuccess(data)
        );

    }
}

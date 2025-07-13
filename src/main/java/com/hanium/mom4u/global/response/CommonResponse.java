package com.hanium.mom4u.global.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private boolean isSuccess;
    private int httpStatus;
    private String message;
    private T data;

    public CommonResponse(boolean isSuccess, int httpStatus, String message, T data) {
        this.isSuccess = isSuccess;
        this.httpStatus = httpStatus;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> onSuccess(final T data) {
        return new CommonResponse<>(true, 200, "success", data);
    }

    public static <T> CommonResponse<Void> onSuccess() {
        return new CommonResponse<>(true, 200, "success", null);
    }

    public static <T> CommonResponse<Void> withMessage(StatusCode statusCode) {
        return new CommonResponse<>(false, 200, statusCode.getDescription(), null);
    }
}

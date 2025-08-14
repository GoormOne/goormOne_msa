package com.example.common.dto;

import com.example.common.exception.CommonCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean isSuccess;
    private int code;
    private String message;
    private T data;

    private ApiResponse(boolean isSuccess, int code, String message, T data) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, CommonCode.SUCCESS.getCode(), CommonCode.SUCCESS.getMessage(), null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, CommonCode.SUCCESS.getCode(), CommonCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(CommonCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

}

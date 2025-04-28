package com.zwj.backend.Response;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Data
@Accessors(chain = true)
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>()
                .setCode(HttpStatus.OK.value())
                .setMessage("success")
                .setData(data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<T>()
                .setCode(code)
                .setMessage(message);
    }
}
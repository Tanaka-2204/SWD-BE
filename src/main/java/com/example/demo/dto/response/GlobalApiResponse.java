package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// Chỉ bao gồm các trường không null trong JSON
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class GlobalApiResponse<T> {

    private int status;
    private String message;
    private T data;

    // Constructor cho các response thành công (luôn có data)
    public static <T> GlobalApiResponse<T> success(int status, String message, T data) {
        return new GlobalApiResponse<>(status, message, data);
    }

    // Constructor cho các response thành công (không có data, ví dụ: 204 No Content)
    public static <T> GlobalApiResponse<T> success(int status, String message) {
        return new GlobalApiResponse<>(status, message, null);
    }

    // Constructor cho các response lỗi (thường không có data)
    public static <T> GlobalApiResponse<T> error(int status, String message) {
        return new GlobalApiResponse<>(status, message, null);
    }
}
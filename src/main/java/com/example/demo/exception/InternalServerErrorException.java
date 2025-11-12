package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends RuntimeException {

    /**
     * Constructor với một thông báo lỗi.
     * @param message Thông báo mô tả lỗi.
     */
    public InternalServerErrorException(String message) {
        super(message);
    }

    /**
     * Constructor với thông báo lỗi và nguyên nhân gốc (Throwable).
     * @param message Thông báo mô tả lỗi.
     * @param cause Lỗi gốc (ví dụ: IOException từ Cloudinary).
     */
    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
package com.example.demo.config;

import com.example.demo.dto.response.GlobalApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice(basePackages = "com.example.demo.controller") // Chỉ áp dụng cho các controller của bạn
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Trả về true để can thiệp vào TẤT CẢ các response
        // (Chúng ta sẽ lọc ra các response đã là GlobalApiResponse ở dưới)
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 1. Nếu body ĐÃ LÀ GlobalApiResponse (từ GlobalExceptionHandler), 
        //    thì không bọc nữa, trả về luôn.
        if (body instanceof GlobalApiResponse) {
            return body;
        }

        // 2. Lấy HTTP status code thực tế (ví dụ: 200, 201, 204)
        int statusCode = ((ServletServerHttpResponse) response).getServletResponse().getStatus();

        // 3. Quyết định message dựa trên status
        String message = "Success";
        if (statusCode == HttpStatus.CREATED.value()) {
            message = "Resource created successfully";
        } else if (statusCode == HttpStatus.NO_CONTENT.value()) {
            message = "Operation successful, no content";
        } else if (statusCode >= 400) {
            // Trường hợp này hiếm khi xảy ra ở đây vì ExceptionHandler đã bắt
            message = "Operation failed";
        }

        // 4. Bọc 'body' (ví dụ: StudentResponseDTO) vào GlobalApiResponse
        return GlobalApiResponse.success(statusCode, message, body);
    }
}
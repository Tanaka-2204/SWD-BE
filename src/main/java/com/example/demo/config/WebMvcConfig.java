// Tạo tệp mới: com/example/demo/config/WebMvcConfig.java
package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

    // Inject resolver của chúng ta
    public WebMvcConfig(AuthPrincipalArgumentResolver authPrincipalArgumentResolver) {
        this.authPrincipalArgumentResolver = authPrincipalArgumentResolver;
    }

    // Thêm resolver vào danh sách resolver của Spring MVC
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authPrincipalArgumentResolver);
    }
}
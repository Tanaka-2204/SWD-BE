package com.example.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Student Loyalty API",
        version = "v1.0.0",
        description = "This API provides endpoints for managing the Student Loyalty Platform, " +
                      "including events, wallets, and user profiles.",
        contact = @Contact(
            name = "Your Name / Team Name",
            email = "your.email@example.com"
            //url = "https://your-project-url.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Local Development Server"
        ),
        @Server(
            url = "https://brachycranic-noncorrelative-joya.ngrok-free.dev",
            description = "Ngrok Public Tunnel"
        )
    },
    // Áp dụng yêu cầu xác thực JWT cho TẤT CẢ các API theo mặc định
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth", // Tên tham chiếu cho cơ chế bảo mật
    description = "JWT authentication token",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
  in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
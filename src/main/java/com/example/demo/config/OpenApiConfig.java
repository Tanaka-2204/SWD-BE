package com.example.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
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
            name = "Hehe",
            email = "khoabdse170432@fpt.edu.vn",
            url = "sche-management.vercel.app"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(
            url = "https://loyalty-system-be.onrender.com",
            description = "Render public"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth", // Tên này vẫn giữ nguyên
    description = "Enter JWT Bearer token",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Không cần nội dung
}
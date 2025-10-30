package com.example.demo.controller;

import com.example.demo.dto.request.TestLoginRequestDTO;
import com.example.demo.dto.response.TestLoginResponseDTO;
import com.example.demo.service.TestLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@Tag(name = "99. Test Utilities", description = "APIs for development and testing (e.g., Swagger login)")
public class TestLoginController {

    private final TestLoginService testLoginService;

    public TestLoginController(TestLoginService testLoginService) {
        this.testLoginService = testLoginService;
    }

    @Operation(
        summary = "Get JWT token for Swagger",
        description = "Logs in a user (Student or Partner) using username/password and returns a JWT. " +
                      "This uses ADMIN_NO_SRP_AUTH flow and is FOR TESTING ONLY."
    )
    @ApiResponse(responseCode = "200", description = "Login successful, token returned")
    @ApiResponse(responseCode = "400", description = "Incorrect username or password")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PostMapping("/login")
    public ResponseEntity<TestLoginResponseDTO> loginForSwagger(
            @Valid @RequestBody TestLoginRequestDTO requestDTO) {
        
        TestLoginResponseDTO response = testLoginService.loginForTest(requestDTO);
        return ResponseEntity.ok(response);
    }
}
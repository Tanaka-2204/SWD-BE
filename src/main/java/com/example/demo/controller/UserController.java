package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Import 3 DTOs để Swagger có thể hiển thị
import com.example.demo.dto.response.AdminResponseDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.dto.response.StudentResponseDTO;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "1. User Profile", description = "APIs for managing the current user's profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Get current user's profile",
        description = "Retrieves the profile for the authenticated user. " +
                      "The response data structure will vary based on the user's role (Student, Partner, or Admin)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Profile retrieved successfully. The body will contain one of the 3 DTO types.",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(oneOf = {
                    StudentResponseDTO.class, 
                    PartnerResponseDTO.class, 
                    AdminResponseDTO.class
                }))
            }
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Profile not found or (for students) profile not completed")
    })
    @GetMapping
    public ResponseEntity<Object> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal
    ) {
        // Gọi service điều phối, nó sẽ trả về 1 trong 3 DTOs
        Object profileDTO = userService.getMyProfile(principal);
        return ResponseEntity.ok(profileDTO);
    }
}
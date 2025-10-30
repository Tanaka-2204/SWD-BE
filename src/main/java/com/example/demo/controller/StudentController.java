package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.StudentProfileCompletionDTO;
import com.example.demo.dto.request.StudentProfileUpdateDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.service.StudentService;
import com.example.demo.exception.ForbiddenException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "Complete student profile after Cognito registration", description = "This endpoint is called once after a new user registers with Cognito to create their corresponding profile in the database. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Phone number already in use or profile already exists")
    })
    @PostMapping("/complete-profile")
    public ResponseEntity<StudentResponseDTO> completeProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal, // <<< SỬA Ở ĐÂY
            @Valid @RequestBody StudentProfileCompletionDTO completionDTO) {

        // Dùng cognitoSub và email trực tiếp từ principal
        StudentResponseDTO newStudent = studentService.completeProfile(
            principal.getCognitoSub(), 
            principal.getEmail(), 
            completionDTO
        );
        return new ResponseEntity<>(newStudent, HttpStatus.CREATED);
    }

    @Operation(summary = "Get current student's profile (Me)", description = "Retrieves the profile details of the currently authenticated student.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved profile"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Student profile not completed"),
            @ApiResponse(responseCode = "404", description = "Student profile not found for authenticated user")
    })
    @GetMapping("/me")
    public ResponseEntity<StudentResponseDTO> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) {
        
        // 1. Kiểm tra xem quá trình phiên dịch có trả về studentId không
        if (principal.getStudentId() == null) {
            // Nếu không có studentId, tức là user đã đăng ký Cognito nhưng chưa gọi complete-profile
            throw new ForbiddenException("Student profile is not completed. Please call /complete-profile first.");
        }
        
        // 2. Gọi service bằng studentId nội bộ đã được phiên dịch
        StudentResponseDTO student = studentService.getStudentById(principal.getStudentId());
        return ResponseEntity.ok(student);
    }

    @Operation(summary = "Update current student's profile", description = "Allows an authenticated student to update their own profile information (e.g., name, phone number, avatar). Email cannot be changed here.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Student profile not found for the authenticated user"),
            @ApiResponse(responseCode = "409", description = "Phone number is already in use by another student")
    })
    @PutMapping("/me")
    public ResponseEntity<StudentResponseDTO> updateMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal, // <<< SỬA Ở ĐÂY
            @Valid @RequestBody StudentProfileUpdateDTO updateDTO) {
        
        // Vẫn dùng cognitoSub để tìm và cập nhật
        StudentResponseDTO updatedStudent = studentService.updateMyProfile(principal.getCognitoSub(), updateDTO);
        return ResponseEntity.ok(updatedStudent);
    }
}
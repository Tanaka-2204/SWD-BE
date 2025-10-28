package com.example.demo.controller;

import com.example.demo.dto.request.StudentProfileCompletionDTO;
import com.example.demo.dto.request.StudentProfileUpdateDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.service.StudentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
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
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StudentProfileCompletionDTO completionDTO) {

        String cognitoSub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        StudentResponseDTO newStudent = studentService.completeProfile(cognitoSub, email, completionDTO);
        return new ResponseEntity<>(newStudent, HttpStatus.CREATED);
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
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StudentProfileUpdateDTO updateDTO) {

        String cognitoSub = jwt.getSubject();
        StudentResponseDTO updatedStudent = studentService.updateMyProfile(cognitoSub, updateDTO);
        return ResponseEntity.ok(updatedStudent);
    }
}
package com.example.demo.controller;

import com.example.demo.dto.StudentProfileUpdateRequest;
import com.example.demo.entity.Student;
import com.example.demo.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Profile", description = "APIs for student profile management")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "Update current student's profile", description = "Allows an authenticated student to update their own profile information (name, avatar, interests).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Student.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized, token is missing or invalid", content = @Content),
            @ApiResponse(responseCode = "404", description = "Student not found in the database", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    public ResponseEntity<Student> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StudentProfileUpdateRequest request) {

        String cognitoSub = jwt.getSubject();

        Student updatedStudent = studentService.updateStudentProfile(cognitoSub, request);

        return ResponseEntity.ok(updatedStudent);
    }
}
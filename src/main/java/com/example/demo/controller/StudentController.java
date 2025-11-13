package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.StudentProfileCompletionDTO;
import com.example.demo.dto.request.StudentProfileUpdateDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.dto.response.EventResponseDTO; 
import com.example.demo.dto.response.PageResponseDTO;
import com.example.demo.service.StudentService;
import com.example.demo.service.EventService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.PageRequest; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.domain.Sort;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "1. Authentication & Profile")
public class StudentController {

    private final StudentService studentService;
    private final EventService eventService;

    public StudentController(StudentService studentService, EventService eventService) {
        this.studentService = studentService;
        this.eventService = eventService;
    }

    @Operation(summary = "Complete student profile (Phone & Avatar)",
               description = "Called once after Cognito registration. Endpoint này nhận 'multipart/form-data'.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data (e.g., phone format)"),
            @ApiResponse(responseCode = "404", description = "University code from JWT not found in DB"),
            @ApiResponse(responseCode = "409", description = "Profile already exists or phone number is taken")
    })
    @PostMapping(value = "/me/complete-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResponseDTO> completeProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            
            // --- SỬA LỖI QUAN TRỌNG TẠI ĐÂY ---
            // Đổi từ @RequestBody (JSON) sang @ModelAttribute (Form-data)
            @Valid @ModelAttribute StudentProfileCompletionDTO completionDTO) {
            // ---------------------------------

        // Lấy raw access token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
             throw new IllegalStateException("Authentication is not a JWT Token");
        }
        String rawAccessToken = ((JwtAuthenticationToken) authentication).getToken().getTokenValue();

        // Service đã được sửa để nhận DTO (chứa MultipartFile)
        StudentResponseDTO newStudent = studentService.completeProfile(
            principal, 
            rawAccessToken, 
            completionDTO
        );
        return new ResponseEntity<>(newStudent, HttpStatus.CREATED);
    }

    @Operation(summary = "Update current student's profile", 
               description = "Allows student to update info. Endpoint này nhận 'multipart/form-data'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Student profile not found"),
            @ApiResponse(responseCode = "409", description = "Phone number is already in use")
    })
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudentResponseDTO> updateMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @ModelAttribute StudentProfileUpdateDTO updateDTO) {
        StudentResponseDTO updatedStudent = studentService.updateMyProfile(principal.getCognitoSub(), updateDTO);
        return ResponseEntity.ok(updatedStudent);
    }

    @Operation(summary = "Get current student's event history", description = "Retrieves a paginated list of events the authenticated student has registered for.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved event history"),
            @ApiResponse(responseCode = "404", description = "Student profile not found")
    })
    @GetMapping("/me/events")
    @PreAuthorize("isAuthenticated() and !hasRole('ADMIN') and !hasRole('PARTNERS')")
    public ResponseEntity<PageResponseDTO<EventResponseDTO>> getMyEventHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkinTime,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        
        // (Sử dụng UUID từ principal)
        Page<EventResponseDTO> events = eventService.getEventHistoryByStudent(principal.getStudentId(), pageable);
        
        return ResponseEntity.ok(new PageResponseDTO<>(events));
    }

    private Pageable createPageable(int page, int size, String sort) {
        int pageIndex = page > 0 ? page - 1 : 0;
        try {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) 
                                        ? Sort.Direction.DESC 
                                        : Sort.Direction.ASC;
            return PageRequest.of(pageIndex, size, Sort.by(direction, sortField));
        } catch (Exception e) {
            return PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "id"));
        }
    }
}
package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.StudentProfileCompletionDTO;
import com.example.demo.dto.request.StudentProfileUpdateDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.dto.response.EventResponseDTO; // <<< THÊM
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
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.data.domain.Page; // <<< THÊM
import org.springframework.data.domain.PageRequest; // <<< THÊM
import org.springframework.data.domain.Pageable; // <<< THÊM
import org.springframework.data.domain.Sort;

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
               description = "Called once after Cognito registration. " + 
                             "Uses fullName and university from JWT, and phone/avatar from request body.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data (e.g., phone format)"),
            @ApiResponse(responseCode = "404", description = "University code from JWT not found in DB"),
            @ApiResponse(responseCode = "409", description = "Profile already exists or phone number is taken")
    })
    // ==========================================================
    // <<< SỬA LỖI 1 (Lỗi 404): Thêm "/me" vào đường dẫn
    // ==========================================================
    @PostMapping("/me/complete-profile")
    public ResponseEntity<StudentResponseDTO> completeProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody StudentProfileCompletionDTO completionDTO) {

        // <<< LẤY RAW ACCESS TOKEN TỪ SECURITY CONTEXT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
             throw new IllegalStateException("Authentication is not a JWT Token");
        }
        String rawAccessToken = ((JwtAuthenticationToken) authentication).getToken().getTokenValue();

        // <<< GỌI SERVICE VỚI TOKEN THÔ
        StudentResponseDTO newStudent = studentService.completeProfile(
            principal, 
            rawAccessToken, // <<< TRUYỀN TOKEN VÀO
            completionDTO
        );
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudentResponseDTO> updateMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal, // <<< SỬA Ở ĐÂY
            @Valid @RequestBody StudentProfileUpdateDTO updateDTO) {
        
        // Vẫn dùng cognitoSub để tìm và cập nhật
        StudentResponseDTO updatedStudent = studentService.updateMyProfile(principal.getCognitoSub(), updateDTO);
        return ResponseEntity.ok(updatedStudent);
    }

    @Operation(summary = "Get current student's event history", description = "Retrieves a paginated list of events the authenticated student has registered for.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved event history"),
            @ApiResponse(responseCode = "404", description = "Student profile not found for the authenticated user")
    })
    @GetMapping("/me/events")
    @PreAuthorize("isAuthenticated() and !hasRole('ADMIN') and !hasRole('PARTNERS')")
    public ResponseEntity<PageResponseDTO<EventResponseDTO>> getMyEventHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkinTime,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        
        // (LƯU Ý: Bạn cần tạo service 'getEventHistoryByStudent')
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
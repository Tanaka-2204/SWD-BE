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
import org.springframework.web.multipart.MultipartFile; 
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.example.demo.exception.BadRequestException;

@RestController
@RequestMapping("/api/v1/students")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "1. Authentication & Profile")
public class StudentController {

    private final StudentService studentService;
    private final EventService eventService;
    private final ObjectMapper objectMapper;

    public StudentController(StudentService studentService, EventService eventService, ObjectMapper objectMapper) {
        this.studentService = studentService;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Complete student profile (Phone & Avatar)", description = "Called once after Cognito registration. Endpoint này nhận 'multipart/form-data'.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data (e.g., phone format)"),
            @ApiResponse(responseCode = "404", description = "University code from JWT not found in DB"),
            @ApiResponse(responseCode = "409", description = "Profile already exists or phone number is taken")
    })
    @PostMapping(value = "/me/complete-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResponseDTO> completeProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,

            @Parameter(description = "JSON data for the profile. Ví dụ: {\"phoneNumber\": \"0901234567\"}", required = true) @RequestPart("data") String completionDTOString,

            @Parameter(description = "Avatar image file (optional)", required = false) @RequestPart(value = "image", required = false) MultipartFile avatarFile) {
        StudentProfileCompletionDTO completionDTO;
        try {
            completionDTO = objectMapper.readValue(completionDTOString, StudentProfileCompletionDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid 'data' JSON format: " + e.getMessage());
        }

        // Lấy raw access token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            throw new IllegalStateException("Authentication is not a JWT Token");
        }
        String rawAccessToken = ((JwtAuthenticationToken) authentication).getToken().getTokenValue();

        // 5. Gọi Service (chúng ta sẽ sửa Service ở bước sau)
        StudentResponseDTO newStudent = studentService.completeProfile(
                principal,
                rawAccessToken,
                completionDTO,
                avatarFile);
        return new ResponseEntity<>(newStudent, HttpStatus.CREATED);
    }

    @Operation(summary = "Update current student's profile", description = "Allows student to update info. Endpoint này nhận 'multipart/form-data'.")
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
            @Parameter(description = "JSON data for the profile. Ví dụ: {\"fullName\": \"Test\"}", required = false) @RequestPart(value = "data", required = false) String updateDTOString,
            @Parameter(description = "New avatar image file (optional)", required = false) @RequestPart(value = "image", required = false) MultipartFile avatarFile) {
        StudentProfileUpdateDTO updateDTO = null;
        if (updateDTOString != null && !updateDTOString.isBlank()) {
            try {
                updateDTO = objectMapper.readValue(updateDTOString, StudentProfileUpdateDTO.class);
            } catch (Exception e) {
                throw new BadRequestException("Invalid 'data' JSON format: " + e.getMessage());
            }
        }
        boolean noData = (updateDTO == null || (updateDTO.getFullName() == null && updateDTO.getPhoneNumber() == null));
        boolean noImage = (avatarFile == null || avatarFile.isEmpty());
        if (noData && noImage) {
            throw new BadRequestException(
                    "No update data provided. Please provide 'data' (JSON) or an 'image' file to update.");
        }
        StudentResponseDTO updatedStudent = studentService.updateMyProfile(
                principal.getCognitoSub(),
                updateDTO,
                avatarFile);
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
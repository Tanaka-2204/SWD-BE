package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.CheckinRequestDTO;
import com.example.demo.dto.request.EventUpdateDTO;
import com.example.demo.dto.response.CheckinResponseDTO;
import com.example.demo.dto.request.EventCreateDTO;
import com.example.demo.dto.request.FeedbackRequestDTO;
import com.example.demo.dto.response.FeedbackResponseDTO;
import com.example.demo.dto.response.EventResponseDTO;
import com.example.demo.dto.response.RegistrationResponseDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.entity.Event;
import com.example.demo.service.CheckinService;
import com.example.demo.service.EventService;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.RegistrationService;
import com.example.demo.util.EventSpecifications;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events") // Đã đổi tiền tố
@Tag(name = "2. Events & Registration")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final CheckinService checkinService;
    private final FeedbackService feedbackService;

    public EventController(EventService eventService, RegistrationService registrationService,
                           CheckinService checkinService, FeedbackService feedbackService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.checkinService = checkinService;
        this.feedbackService = feedbackService;
    }

    // == CRUD Endpoints ==

    @Operation(
        summary = "Get all events with optional filters",
        description = "Returns a paginated list of events. Can be filtered by categoryId and/or status. Public endpoint."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @SecurityRequirement(name = "bearerAuth", scopes = {}) // Public
    @GetMapping
    public ResponseEntity<Page<EventResponseDTO>> getAllEvents(
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by status (e.g., ACTIVE, FINISHED)") @RequestParam(required = false) String status,
            Pageable pageable) {

        Specification<Event> spec = EventSpecifications.filterBy(categoryId, status);
        Page<EventResponseDTO> events = eventService.getAllEvents(spec, pageable);
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Get event by ID", description = "Returns details for a specific event. This is a public endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved event"),
            @ApiResponse(responseCode = "404", description = "Event not found with the given ID")
    })
    @SecurityRequirement(name = "bearerAuth", scopes = {}) // Public endpoint
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(
            @Parameter(description = "ID of the event to retrieve") @PathVariable Long id) {
        EventResponseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @Operation(summary = "Create a new event", description = "Endpoint for an authenticated partner or admin to create a new event. PartnerId must be specified in the request body.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Event created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid event data provided"),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission or Partner mismatch"),
        @ApiResponse(responseCode = "404", description = "Partner or Category specified not found")
    })
    @PostMapping // Đổi endpoint thành POST /events
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNERS')") // Chỉ Admin hoặc Partner được gọi
    public ResponseEntity<EventResponseDTO> createEvent(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt, // Inject JWT
            @Valid @RequestBody EventCreateDTO requestDTO) { // DTO chứa partnerId

        EventResponseDTO createdEvent = eventService.createEvent(jwt, requestDTO); // Gọi service mới
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing event", description = "Allows the organizing partner or an admin to update event details.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "403", description = "User does not have permission to update this event"),
        @ApiResponse(responseCode = "404", description = "Event or Category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @Parameter(description = "ID of the event to update") @PathVariable Long id,
            @Valid @RequestBody EventUpdateDTO requestDTO) {
        // TODO: Thêm logic kiểm tra quyền (@PreAuthorize hoặc trong service)
        EventResponseDTO updatedEvent = eventService.updateEvent(id, requestDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @Operation(summary = "Delete an event", description = "Allows the organizing partner or an admin to delete an event.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
        @ApiResponse(responseCode = "403", description = "User does not have permission to delete this event"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID of the event to delete") @PathVariable Long id) {
        // TODO: Thêm logic kiểm tra quyền (@PreAuthorize hoặc trong service)
        // TODO: Cân nhắc logic nghiệp vụ (không cho xóa sự kiện đã/đang diễn ra?) trong service
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }


    // == Business Logic Endpoints ==

    // Endpoint POST /partners/{partnerId}/events nằm trong PartnerController

    @Operation(
        summary = "Student registers for an event",
        description = "Allows an authenticated student to register for a specific event."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Successfully registered"),
        @ApiResponse(responseCode = "404", description = "Event or Student not found"),
        @ApiResponse(responseCode = "409", description = "Already registered")
    })
    @PostMapping("/{eventId}/register")
    public ResponseEntity<RegistrationResponseDTO> registerForEvent(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal, // <<< SỬA Ở ĐÂY
            @Parameter(description = "ID of the event to register for") @PathVariable Long eventId) {
        
        // KIỂM TRA: API này yêu cầu phải complete-profile
        if (principal.getStudentId() == null) {
            throw new ForbiddenException("Student profile is not completed. Please call /api/students/complete-profile first.");
        }
        
        // Service bây giờ chỉ cần studentId, không cần biết cognitoSub
        RegistrationResponseDTO registration = registrationService.createRegistration(principal.getStudentId(), eventId);
        return new ResponseEntity<>(registration, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Check-in student and grant reward",
        description = "Organizer check-in a student by phone. Triggers reward transaction if criteria are met."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check-in successful"),
        @ApiResponse(responseCode = "404", description = "Event or Student not found"),
        @ApiResponse(responseCode = "409", description = "Student already checked in")
    })
    @PostMapping("/{eventId}/checkin") 
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<CheckinResponseDTO> checkInStudent(
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            @Valid @RequestBody CheckinRequestDTO requestDTO) {
        CheckinResponseDTO checkinResponse = checkinService.performCheckin(eventId, requestDTO);
        return ResponseEntity.ok(checkinResponse);
    }

    @Operation(
        summary = "Get list of event attendees",
        description = "Returns a paginated list of students registered for the event."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved attendees")
    @GetMapping("/{eventId}/attendees")
    public ResponseEntity<Page<StudentResponseDTO>> getEventAttendees(
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            Pageable pageable) {
        Page<StudentResponseDTO> attendees = registrationService.getAttendeesByEvent(eventId, pageable);
        return ResponseEntity.ok(attendees);
    }

    @Operation(
        summary = "Student submits feedback for an event",
        description = "Allows an authenticated student to submit rating and comments for an event they attended."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Feedback submitted successfully"),
        @ApiResponse(responseCode = "403", description = "Student did not register for this event / Profile not completed"), // Sửa mô tả
        @ApiResponse(responseCode = "409", description = "Feedback already submitted")
    })
    @PostMapping("/{eventId}/feedback")
    public ResponseEntity<FeedbackResponseDTO> submitFeedback(
            // SỬA Ở ĐÂY: Dùng AuthPrincipal thay vì Jwt
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal, 
            
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            @Valid @RequestBody FeedbackRequestDTO requestDTO) {
        
        // 1. Kiểm tra xem user đã "complete-profile" (hoàn tất hồ sơ) chưa
        // Nếu getStudentId() trả về null, nghĩa là họ chưa hoàn tất.
        if (principal.getStudentId() == null) {
            throw new ForbiddenException("Student profile is not completed. Please call /api/students/complete-profile first.");
        }

        // 2. Lấy ID nội bộ (BE ID) từ principal
        Long studentId = principal.getStudentId(); 

        // 3. Gọi service với ID nội bộ (studentId)
        // Dòng này bây giờ sẽ chạy đúng
        FeedbackResponseDTO feedbackResponse = feedbackService.createFeedback(studentId, eventId, requestDTO);
        
        return new ResponseEntity<>(feedbackResponse, HttpStatus.CREATED);
    }
}
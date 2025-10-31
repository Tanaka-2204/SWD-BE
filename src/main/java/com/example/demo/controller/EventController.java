package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.CheckinRequestDTO;
import com.example.demo.dto.request.EventUpdateDTO;
import com.example.demo.dto.response.CheckinResponseDTO;
import com.example.demo.dto.request.EventCreateDTO;
import com.example.demo.dto.request.FeedbackRequestDTO;
import com.example.demo.dto.response.FeedbackResponseDTO;
import com.example.demo.dto.response.EventResponseDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.entity.Event;
import com.example.demo.service.CheckinService;
import com.example.demo.service.EventService;
import com.example.demo.service.FeedbackService;
// import com.example.demo.service.RegistrationService; // <<< ĐÃ XÓA
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
@RequestMapping("/api/v1/events") 
@Tag(name = "2. Events & Registration")
public class EventController {

    private final EventService eventService;
    private final CheckinService checkinService;
    private final FeedbackService feedbackService;

    // <<< SỬA CONSTRUCTOR (Đã xóa RegistrationService)
    public EventController(EventService eventService, 
                           CheckinService checkinService, 
                           FeedbackService feedbackService) {
        this.eventService = eventService;
        this.checkinService = checkinService;
        this.feedbackService = feedbackService;
    }

    // == CRUD Endpoints ==

    @Operation(summary = "Get all events with optional filters")
    @GetMapping
    public ResponseEntity<Page<EventResponseDTO>> getAllEvents(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        Specification<Event> spec = EventSpecifications.filterBy(categoryId, status);
        Page<EventResponseDTO> events = eventService.getAllEvents(spec, pageable);
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Get event by ID")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        EventResponseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @Operation(summary = "Create a new event")
    @PostMapping 
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNERS')") 
    public ResponseEntity<EventResponseDTO> createEvent(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt, 
            @Valid @RequestBody EventCreateDTO requestDTO) { 

        EventResponseDTO createdEvent = eventService.createEvent(jwt, requestDTO); 
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing event")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNER')") // <<< SỬA: Thêm PreAuthorize
    public ResponseEntity<EventResponseDTO> updateEvent(
            @Parameter(description = "ID of the event to update") @PathVariable Long id,
            @Valid @RequestBody EventUpdateDTO requestDTO,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) { // <<< SỬA: Thêm Principal
        
        // Gọi service đã cập nhật
        EventResponseDTO updatedEvent = eventService.updateEvent(id, requestDTO, principal);
        return ResponseEntity.ok(updatedEvent);
    }

    @Operation(summary = "Delete an event")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNER')") // <<< SỬA: Thêm PreAuthorize
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID of the event to delete") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) { // <<< SỬA: Thêm Principal
        
        // Gọi service đã cập nhật
        eventService.deleteEvent(id, principal);
        return ResponseEntity.noContent().build();
    }

    // == Business Logic Endpoints ==

    @Operation(summary = "Student registers for an event")
    @PostMapping("/{eventId}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CheckinResponseDTO> registerEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthPrincipal principal) { 

        String cognitoSub = principal.getCognitoSub();
        CheckinResponseDTO response = checkinService.registerEvent(cognitoSub, eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check-in student (by Partner/Admin)")
    @PostMapping("/{eventId}/checkin") 
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNER')") // <<< SỬA: Đổi PreAuthorize
    public ResponseEntity<CheckinResponseDTO> checkInStudent(
            @PathVariable Long eventId,
            @Valid @RequestBody CheckinRequestDTO requestDTO,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) { // <<< SỬA: Thêm Principal
        
        // Gọi service đã cập nhật
        CheckinResponseDTO checkinResponse = checkinService.performCheckin(eventId, requestDTO, principal);
        return ResponseEntity.ok(checkinResponse);
    }

    @Operation(summary = "Get list of event attendees")
    @GetMapping("/{eventId}/attendees")
    public ResponseEntity<Page<StudentResponseDTO>> getEventAttendees(
            @PathVariable Long eventId,
            Pageable pageable) {
        
        // (Logic này đã đúng, gọi CheckinService)
        Page<StudentResponseDTO> attendees = checkinService.getAttendeesByEvent(eventId, pageable);
        return ResponseEntity.ok(attendees);
    }

    @Operation(summary = "Student submits feedback for an event")
    @PostMapping("/{eventId}/feedback")
    @PreAuthorize("hasRole('STUDENT')") // <<< SỬA: Thêm PreAuthorize
    public ResponseEntity<FeedbackResponseDTO> submitFeedback(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            @Valid @RequestBody FeedbackRequestDTO requestDTO) {

        if (principal.getStudentId() == null) {
            throw new ForbiddenException(
                    "Student profile is not completed. Please call /api/students/complete-profile first.");
        }
        Long studentId = principal.getStudentId();
        FeedbackResponseDTO feedbackResponse = feedbackService.createFeedback(studentId, eventId, requestDTO);
        return new ResponseEntity<>(feedbackResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Finalize event and payout rewards (by Partner/Admin)")
    @PostMapping("/{eventId}/finalize")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNER')") 
    public ResponseEntity<EventResponseDTO> finalizeEvent(
            @Parameter(description = "ID of the event to finalize") @PathVariable Long eventId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) { // <<< SỬA: Thêm Principal
        
        // Gọi service đã cập nhật
        EventResponseDTO finalizedEvent = eventService.finalizeEvent(eventId, principal);
        
        return ResponseEntity.ok(finalizedEvent);
    }
}
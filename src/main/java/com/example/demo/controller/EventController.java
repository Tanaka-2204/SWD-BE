package com.example.demo.controller;

import com.example.demo.dto.request.CheckinRequestDTO;
import com.example.demo.dto.request.EventUpdateDTO;
import com.example.demo.dto.response.CheckinResponseDTO;
import com.example.demo.dto.request.EventCreateDTO;
import com.example.demo.dto.request.FeedbackRequestDTO;
import com.example.demo.dto.response.FeedbackResponseDTO;
import com.example.demo.dto.response.EventResponseDTO;
import com.example.demo.dto.response.RegistrationResponseDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.entity.Event;
import com.example.demo.service.CheckinService;
import com.example.demo.service.EventService;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.RegistrationService;
import com.example.demo.util.EventSpecifications;
// import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt; // XÓA DÒNG NÀY
import org.springframework.security.oauth2.jwt.Jwt; // THÊM IMPORT ĐÚNG
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
@Tag(name = "6. Event", description = "APIs for creating and managing events")
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

    // Endpoint POST /partners/{partnerId}/events nằm trong PartnerController

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
    @PostMapping("/{eventId}/register") // GIỮ LẠI PHIÊN BẢN NÀY
    public ResponseEntity<RegistrationResponseDTO> registerForEvent(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt, // Đã sửa import
            @Parameter(description = "ID of the event to register for") @PathVariable Long eventId) {
        String cognitoSub = jwt.getSubject(); // Sẽ hoạt động
        RegistrationResponseDTO registration = registrationService.createRegistration(cognitoSub, eventId);
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
    @PostMapping("/{eventId}/checkin") // GIỮ LẠI PHIÊN BẢN NÀY
    public ResponseEntity<CheckinResponseDTO> checkInStudent(
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            @Valid @RequestBody CheckinRequestDTO requestDTO) {
        // TODO: Thêm @PreAuthorize("hasRole('PARTNER') or hasRole('ADMIN')")
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
        @ApiResponse(responseCode = "403", description = "Student did not register for this event"),
        @ApiResponse(responseCode = "409", description = "Feedback already submitted")
    })
    @PostMapping("/{eventId}/feedback")
    public ResponseEntity<FeedbackResponseDTO> submitFeedback(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt, // Đã sửa import
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            @Valid @RequestBody FeedbackRequestDTO requestDTO) {
        String cognitoSub = jwt.getSubject(); // Sẽ hoạt động
        FeedbackResponseDTO feedbackResponse = feedbackService.createFeedback(cognitoSub, eventId, requestDTO);
        return new ResponseEntity<>(feedbackResponse, HttpStatus.CREATED);
    }
}
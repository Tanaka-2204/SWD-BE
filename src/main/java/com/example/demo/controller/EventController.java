package com.example.demo.controller;

import com.example.demo.dto.request.EventCreateDTO;
import com.example.demo.dto.response.EventResponseDTO;
import com.example.demo.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "6. Event", description = "APIs for creating and managing events")
public class EventController {

    private final EventService eventService;
    // Lưu ý: Bạn sẽ cần inject các service khác ở đây khi triển khai các API nghiệp
    // vụ
    // private final RegistrationService registrationService;
    // private final CheckinService checkinService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // == CRUD Endpoints ==

    @Operation(summary = "Get all events", description = "Returns a paginated list of all events. This is a public endpoint.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of events")
    @SecurityRequirement(name = "bearerAuth", scopes = {}) // Public endpoint
    @GetMapping("/events")
    public ResponseEntity<?> getAllEvents(Pageable pageable) {
        var eventsPage = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(eventsPage);
    }

    @Operation(summary = "Get event by ID", description = "Returns details for a specific event. This is a public endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved event"),
            @ApiResponse(responseCode = "404", description = "Event not found with the given ID")
    })
    @SecurityRequirement(name = "bearerAuth", scopes = {}) // Public endpoint
    @GetMapping("/events/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(
            @Parameter(description = "ID of the event to retrieve") @PathVariable Long id) {
        // TODO: Triển khai logic lấy sự kiện theo ID trong EventService
        EventResponseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    // == Business Logic Endpoints ==

    @Operation(summary = "Create a new event for a partner", description = "Endpoint for an authenticated partner to create a new event. The partner is identified by the path variable.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid event data provided"),
            @ApiResponse(responseCode = "404", description = "Partner not found")
    })
    @PostMapping("/partners/{partnerId}/events")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Parameter(description = "ID of the partner creating the event") @PathVariable Long partnerId,
            @Valid @RequestBody EventCreateDTO requestDTO) {

        EventResponseDTO createdEvent = eventService.createEvent(partnerId, requestDTO);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @Operation(summary = "Student registers for an event", description = "Allows an authenticated student to register for a specific event. Student ID is taken from the JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successfully registered for the event"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Student is already registered for this event")
    })
    @PostMapping("/events/{eventId}/register")
    public ResponseEntity<Void> registerForEvent(
            // @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt, // Lấy student từ
            // token
            @Parameter(description = "ID of the event to register for") @PathVariable Long eventId) {
        // String cognitoSub = jwt.getSubject();
        // registrationService.createRegistration(cognitoSub, eventId);
        // TODO: Triển khai logic đăng ký trong một service riêng (ví dụ:
        // RegistrationService)
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Check-in a student to an event", description = "Allows an organizer/partner to check-in a student using their phone number.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check-in successful"),
            @ApiResponse(responseCode = "404", description = "Event or student with phone number not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., student not registered)")
    })
    @PostMapping("/events/{eventId}/checkin")
    public ResponseEntity<Void> checkInStudent(
            @Parameter(description = "ID of the event") @PathVariable Long eventId,
            @RequestBody String phoneNumber) { // Cần một DTO cho phoneNumber
        // checkinService.performCheckin(eventId, phoneNumber);
        // TODO: Triển khai logic check-in trong một service riêng (ví dụ:
        // CheckinService)
        return ResponseEntity.ok().build();
    }
}
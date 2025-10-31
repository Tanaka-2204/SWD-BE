package com.example.demo.controller;

import com.example.demo.dto.request.BroadcastRequestDTO;
import com.example.demo.dto.request.EventCategoryRequestDTO;
import com.example.demo.dto.request.PartnerRequestDTO;
import com.example.demo.dto.request.UniversityRequestDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.request.WalletTopupRequestDTO;
import com.example.demo.dto.response.EventBroadcastResponseDTO;
import com.example.demo.dto.response.EventResponseDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.service.StudentService;
import com.example.demo.dto.response.EventCategoryResponseDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.dto.response.UniversityResponseDTO;
import com.example.demo.dto.response.WalletTransactionResponseDTO;
import com.example.demo.service.BroadcastService;
import com.example.demo.service.EventCategoryService;
import com.example.demo.service.EventService;
import com.example.demo.service.PartnerService;
import com.example.demo.service.UniversityService;
import com.example.demo.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "1. Admin Management", description = "APIs for administrative tasks")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final PartnerService partnerService;
    private final WalletService walletService;
    private final EventCategoryService eventCategoryService;
    private final StudentService studentService;
    private final UniversityService universityService;
    private final EventService eventService;
    private final BroadcastService broadcastService;

    public AdminController(PartnerService partnerService,
                            WalletService walletService,
                            EventCategoryService eventCategoryService,
                            StudentService studentService,
                            UniversityService universityService,
                            EventService eventService,
                            BroadcastService broadcastService) {
        this.partnerService = partnerService;
        this.walletService = walletService;
        this.eventCategoryService = eventCategoryService;
        this.studentService = studentService;
        this.universityService = universityService;
        this.eventService = eventService;
        this.broadcastService = broadcastService;
    }

    // ===================================
    // == Partner Management
    // ===================================

    @Operation(summary = "Admin creates a new partner", description = "Creates a new partner, associated wallet, and Cognito user linkage.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Partner created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Partner name or Cognito user/email already exists")
    })
    @PostMapping("/partners")
    public ResponseEntity<PartnerResponseDTO> createPartner(@Valid @RequestBody PartnerRequestDTO requestDTO) {
        // Service này đã chứa logic tạo user Cognito và add group
        PartnerResponseDTO newPartner = partnerService.createPartner(requestDTO); 
        return new ResponseEntity<>(newPartner, HttpStatus.CREATED);
    }

    @Operation(summary = "Admin gets list of all partners", description = "Retrieves a complete list of all partners in the system.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved partner list")
    @GetMapping("/partners")
    public ResponseEntity<List<PartnerResponseDTO>> getAllPartners() {
        List<PartnerResponseDTO> partners = partnerService.getAllPartners();
        return ResponseEntity.ok(partners);
    }
    
    // (Bạn cũng có thể di chuyển API PUT/DELETE Partner vào đây nếu chỉ Admin được làm)

    // ===================================
    // == Wallet Management
    // ===================================

    @Operation(summary = "Admin top up coin for a partner", description = "Adds funds to a specific partner's wallet.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Top-up successful"),
        @ApiResponse(responseCode = "404", description = "Partner or wallets not found")
    })
    @PostMapping("/wallets/topup")
    public ResponseEntity<WalletTransactionResponseDTO> topupWalletForPartner(
            @Valid @RequestBody WalletTopupRequestDTO topupRequest) {
        WalletTransactionResponseDTO transaction = walletService.adminTopupForPartner(topupRequest);
        return ResponseEntity.ok(transaction);
    }

    // ===================================
    // == Event Category Management
    // ===================================

    @Operation(summary = "Admin creates a new event category", description = "Adds a new category for classifying events.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "409", description = "Category with the same name already exists")
    })
    @PostMapping("/event-categories")
    public ResponseEntity<EventCategoryResponseDTO> createCategory(
            @Valid @RequestBody EventCategoryRequestDTO requestDTO) {
        EventCategoryResponseDTO newCategory = eventCategoryService.createCategory(requestDTO);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    @Operation(summary = "Admin updates an event category", description = "Updates the details of an existing event category.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "404", description = "Event category not found"),
        @ApiResponse(responseCode = "409", description = "Another category with the same name already exists")
    })
    @PutMapping("/event-categories/{id}")
    public ResponseEntity<EventCategoryResponseDTO> updateCategory(
            @Parameter(description = "ID of the category to update") @PathVariable Long id,
            @Valid @RequestBody EventCategoryRequestDTO requestDTO) {
        return ResponseEntity.ok(eventCategoryService.updateCategory(id, requestDTO));
    }

    @Operation(summary = "Admin deletes an event category", description = "Deletes an event category.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/event-categories/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID of the category to delete") @PathVariable Long id) {
        eventCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ===================================
    // == User Management
    // ===================================

    @Operation(summary = "Admin gets list of all students", description = "Retrieves a paginated list of all student users.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved student list")
    @GetMapping("/students")
    public ResponseEntity<Page<StudentResponseDTO>> getAllStudents(Pageable pageable) {
        Page<StudentResponseDTO> students = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(students);
    }

    @Operation(summary = "Admin updates a student's status", description = "Updates the status of a specific student (e.g., to 'ACTIVE' or 'SUSPENDED').")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status value provided"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PatchMapping("/students/{id}/status") // <<< API MỚI
    public ResponseEntity<StudentResponseDTO> updateStudentStatus(
            @Parameter(description = "ID of the student to update") @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateDTO statusDTO) {
        
        StudentResponseDTO updatedStudent = studentService.updateStudentStatus(id, statusDTO);
        return ResponseEntity.ok(updatedStudent);
    }

    @Operation(summary = "Admin updates a partner's status", description = "Updates the status of a specific partner (e.g., to 'ACTIVE' or 'SUSPENDED').")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Partner status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status value provided"),
        @ApiResponse(responseCode = "404", description = "Partner not found")
    })
    @PatchMapping("/partners/{id}/status") // <<< API MỚI
    public ResponseEntity<PartnerResponseDTO> updatePartnerStatus(
            @Parameter(description = "ID of the partner to update") @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateDTO statusDTO) {

        PartnerResponseDTO updatedPartner = partnerService.updatePartnerStatus(id, statusDTO);
        return ResponseEntity.ok(updatedPartner);
    }

    // ===================================
    // == University Management
    // ===================================

    @Operation(summary = "Admin creates a new university", description = "Adds a new university to the system.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "University created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "University with the same name already exists")
    })
    @PostMapping("/universities")
    public ResponseEntity<UniversityResponseDTO> createUniversity(@Valid @RequestBody UniversityRequestDTO requestDTO) {
        UniversityResponseDTO newUniversity = universityService.createUniversity(requestDTO);
        return new ResponseEntity<>(newUniversity, HttpStatus.CREATED);
    }

    // ===================================
    // == Event Management
    // ===================================

    @Operation(summary = "Admin approves or rejects an event", description = "Updates the status of an event to APPROVED or REJECTED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    @PatchMapping("/events/{id}/status")
    public ResponseEntity<EventResponseDTO> updateEventStatus(
            @Parameter(description = "ID of the event to update") @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateDTO statusDTO) {

        EventResponseDTO updatedEvent = eventService.updateEventStatus(id, statusDTO.getStatus());
        return ResponseEntity.ok(updatedEvent);
    }

    // ===================================
    // == Transaction Monitoring
    // ===================================

    @Operation(summary = "Admin gets all transactions", description = "Retrieves a paginated list of all wallet transactions in the system for monitoring.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction list")
    @GetMapping("/transactions")
    public ResponseEntity<Page<WalletTransactionResponseDTO>> getAllTransactions(Pageable pageable) {
        Page<WalletTransactionResponseDTO> transactions = walletService.getAllTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    // ===================================
    // == Broadcast Management
    // ===================================

    @Operation(summary = "Admin sends system-wide broadcast", description = "Sends a broadcast message to all students in the system.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Broadcast sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid broadcast data")
    })
    @PostMapping("/broadcast")
    public ResponseEntity<EventBroadcastResponseDTO> sendSystemBroadcast(@Valid @RequestBody BroadcastRequestDTO requestDTO) {
        EventBroadcastResponseDTO broadcast = broadcastService.sendSystemBroadcast(requestDTO);
        return ResponseEntity.ok(broadcast);
    }
}

package com.example.demo.controller;

import com.example.demo.dto.request.EventCategoryRequestDTO;
import com.example.demo.dto.request.PartnerRequestDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.request.WalletTopupRequestDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.dto.request.UniversityRequestDTO; // <<< THÊM
import com.example.demo.dto.response.UniversityResponseDTO;
import com.example.demo.service.StudentService;
import com.example.demo.service.UniversityService;
import com.example.demo.dto.response.EventCategoryResponseDTO;
import com.example.demo.dto.response.FeedbackResponseDTO;
import com.example.demo.dto.response.PageResponseDTO;
import com.example.demo.dto.response.EventResponseDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.dto.response.WalletTransactionResponseDTO;
import com.example.demo.service.EventCategoryService;
import com.example.demo.service.EventService;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.PartnerService;
import com.example.demo.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "2. Admin Management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final PartnerService partnerService;
    private final WalletService walletService;
    private final EventCategoryService eventCategoryService;
    private final EventService eventService;
    private final StudentService studentService;
    private final UniversityService universityService;
    private final FeedbackService feedbackService;

    public AdminController(PartnerService partnerService,
                           WalletService walletService,
                           EventCategoryService eventCategoryService,
                           EventService eventService,
                           StudentService studentService, UniversityService universityService, FeedbackService feedbackService) {
        this.partnerService = partnerService;
        this.walletService = walletService;
        this.eventCategoryService = eventCategoryService;
        this.eventService = eventService;
        this.studentService = studentService;
        this.universityService = universityService;
        this.feedbackService = feedbackService;
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

    @Operation(summary = "Admin gets list of all partners", description = "Retrieves a paginated list of all partners.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved partner list")
    @GetMapping("/partners")
    public ResponseEntity<PageResponseDTO<PartnerResponseDTO>> getAllPartners(
            // FE chỉ cần gửi 3 tham số đơn giản này
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        // Backend tự xử lý logic Pageable phức tạp
        Pageable pageable = createPageable(page, size, sort);
        
        // Gọi service (Lưu ý: Service phải được cập nhật ở Bước 3)
        Page<PartnerResponseDTO> partnersPage = partnerService.getAllPartners(pageable);
        
        // Trả về DTO đã được tối giản
        return ResponseEntity.ok(new PageResponseDTO<>(partnersPage));
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
            @Parameter(description = "ID of the category to update") @PathVariable UUID id,
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
            @Parameter(description = "ID of the category to delete") @PathVariable UUID id) {
        eventCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ===================================
    // == User Management
    // ===================================

    @Operation(summary = "Admin gets list of all students", description = "Retrieves a paginated list of all student users.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved student list")
    @GetMapping("/students")
    public ResponseEntity<PageResponseDTO<StudentResponseDTO>> getAllStudents(
            // FE chỉ cần gửi 3 tham số đơn giản này
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        // Backend tự xử lý logic Pageable phức tạp
        Pageable pageable = createPageable(page, size, sort);
        
        // Gọi service
        Page<StudentResponseDTO> students = studentService.getAllStudents(pageable);
        
        // Trả về DTO đã được tối giản
        return ResponseEntity.ok(new PageResponseDTO<>(students));
    }

    @Operation(summary = "Admin updates a student's status", description = "Updates the status of a specific student (e.g., to 'ACTIVE' or 'SUSPENDED').")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status value provided"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PatchMapping("/students/{id}/status") // <<< API MỚI
    public ResponseEntity<StudentResponseDTO> updateStudentStatus(
            @Parameter(description = "ID of the student to update") @PathVariable UUID id,
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
            @Parameter(description = "ID of the partner to update") @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateDTO statusDTO) {
        
        PartnerResponseDTO updatedPartner = partnerService.updatePartnerStatus(id, statusDTO);
        return ResponseEntity.ok(updatedPartner);
    }

    @Operation(summary = "Admin creates a new university", description = "ADMIN only.")
    @ApiResponse(responseCode = "201", description = "University created successfully")
    @PostMapping("/universities") // <<< API MỚI
    public ResponseEntity<UniversityResponseDTO> createUniversity(
            @Valid @RequestBody UniversityRequestDTO dto) {
        UniversityResponseDTO createdUniversity = universityService.createUniversity(dto);
        return new ResponseEntity<>(createdUniversity, HttpStatus.CREATED);
    }

    @Operation(summary = "Admin updates a university", description = "ADMIN only.")
    @ApiResponse(responseCode = "200", description = "University updated successfully")
    @PutMapping("/universities/{id}") // <<< API MỚI
    public ResponseEntity<UniversityResponseDTO> updateUniversity(
            @PathVariable UUID id,
            @Valid @RequestBody UniversityRequestDTO dto) {
        UniversityResponseDTO updatedUniversity = universityService.updateUniversity(id, dto);
        return ResponseEntity.ok(updatedUniversity);
    }

    @Operation(summary = "Admin deletes a university", description = "ADMIN only. Fails if any student is linked.")
    @ApiResponse(responseCode = "204", description = "University deleted successfully")
    @DeleteMapping("/universities/{id}") // <<< API MỚI
    public ResponseEntity<Void> deleteUniversity(@PathVariable UUID id) {
        universityService.deleteUniversity(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Admin gets all feedback (system-wide)",
               description = "Retrieves a paginated list of all feedback. Can be filtered by eventId.")
    @GetMapping("/feedback")
    public ResponseEntity<PageResponseDTO<FeedbackResponseDTO>> getAllSystemFeedback(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Pageable pageable = createPageable(1, size, sort);
        Page<FeedbackResponseDTO> feedbackPage = feedbackService.getAllFeedback(eventId, pageable);
        
        return ResponseEntity.ok(new PageResponseDTO<>(feedbackPage));
    }

    @Operation(summary = "Admin gets all wallet transactions", description = "Retrieves a paginated list of all wallet transactions in the system.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions")
    @GetMapping("/wallets/transactions")
    public ResponseEntity<PageResponseDTO<WalletTransactionResponseDTO>> getAllTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) { // <<< SỬA 1: Dùng @RequestParam

        // SỬA 2: Gọi helper 'createPageable'
        Pageable pageable = createPageable(page, size, sort);
        
        Page<WalletTransactionResponseDTO> transactions = walletService.getAllTransactions(pageable);
        
        // SỬA 3: Trả về PageResponseDTO
        return ResponseEntity.ok(new PageResponseDTO<>(transactions));
    }

    @Operation(summary = "Admin approves a pending event", description = "Changes event status from PENDING to APPROVED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event approved successfully"),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "409", description = "Event is not in PENDING status")
    })
    @PatchMapping("/events/{id}/approve")
    public ResponseEntity<EventResponseDTO> approveEvent(@PathVariable UUID id) {
        EventResponseDTO approvedEvent = eventService.approveEvent(id);
        return ResponseEntity.ok(approvedEvent);
    }

    private Pageable createPageable(int page, int size, String sort) {
        int pageIndex = page > 0 ? page - 1 : 0; // Chuyển 1-based (FE) về 0-based (Spring)

        try {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) 
                                        ? Sort.Direction.DESC 
                                        : Sort.Direction.ASC;
            
            return PageRequest.of(pageIndex, size, Sort.by(direction, sortField));
        } catch (Exception e) {
            // (Xử lý lỗi nếu chuỗi sort bị sai, ví dụ dùng 'id' làm mặc định)
            return PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.ASC, "id"));
        }
    }
}

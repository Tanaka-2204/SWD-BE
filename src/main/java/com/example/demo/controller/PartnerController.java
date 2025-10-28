package com.example.demo.controller;

import com.example.demo.dto.request.PartnerRequestDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.dto.response.EventResponseDTO; 
import com.example.demo.service.EventService; 
import com.example.demo.dto.response.WalletResponseDTO; 
import com.example.demo.dto.response.WalletTransactionResponseDTO; 
import com.example.demo.service.WalletService;
import com.example.demo.dto.request.EventFundingRequestDTO; 
import com.example.demo.dto.response.EventFundingResponseDTO; 
import com.example.demo.service.EventFundingService;
import com.example.demo.dto.request.BroadcastRequestDTO; // Thêm
import com.example.demo.dto.response.EventBroadcastResponseDTO; // Thêm
import com.example.demo.service.BroadcastService;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;
import com.example.demo.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partners")
public class PartnerController {

    private final PartnerService partnerService;
    private final EventService eventService;
    private final WalletService walletService;
    private final EventFundingService eventFundingService;
    private final BroadcastService broadcastService;

    public PartnerController(PartnerService partnerService, EventService eventService, WalletService walletService, EventFundingService eventFundingService, BroadcastService broadcastService) {
        this.partnerService = partnerService;
        this.eventService = eventService;
        this.walletService = walletService;
        this.eventFundingService = eventFundingService;
        this.broadcastService = broadcastService;
    }

    @Operation(summary = "Admin creates a new partner", description = "Creates a new partner and an associated wallet. This endpoint requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Partner created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden: User does not have ADMIN role"),                                                                                          
            @ApiResponse(responseCode = "409", description = "Partner with the same name already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerResponseDTO> createPartner(@Valid @RequestBody PartnerRequestDTO requestDTO) {
        PartnerResponseDTO newPartner = partnerService.createPartner(requestDTO);
        return new ResponseEntity<>(newPartner, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a partner by ID", description = "Retrieves details of a specific partner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved partner"),
            @ApiResponse(responseCode = "404", description = "Partner not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PartnerResponseDTO> getPartnerById(
            @Parameter(description = "ID of the partner to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(partnerService.getPartnerById(id));
    }

    @Operation(summary = "Get Partner's organized events", description = "Returns a paginated list of all events organized by this partner.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved event list"),
        @ApiResponse(responseCode = "404", description = "Partner not found")
    })
    @GetMapping("/{partnerId}/events")
    public ResponseEntity<Page<EventResponseDTO>> getPartnerEvents(
            @Parameter(description = "ID of the partner") @PathVariable Long partnerId,
            Pageable pageable) {
        Page<EventResponseDTO> events = eventService.getEventsByPartner(partnerId, pageable);
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Get partner's wallet details", description = "Retrieves the wallet information for a specific partner.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved wallet"),
        @ApiResponse(responseCode = "404", description = "Partner or wallet not found")
    })
    @GetMapping("/{partnerId}/wallet")
    public ResponseEntity<WalletResponseDTO> getPartnerWallet(
            @Parameter(description = "ID of the partner") @PathVariable Long partnerId) {
        // Dùng lại phương thức đã tạo trong WalletService
        WalletResponseDTO wallet = walletService.getWalletByOwner("PARTNER", partnerId);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Get partner's wallet transaction history", description = "Retrieves a paginated list of transactions for a partner's wallet.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction history"),
        @ApiResponse(responseCode = "404", description = "Partner or wallet not found")
    })
    @GetMapping("/{partnerId}/wallet/history")
    public ResponseEntity<Page<WalletTransactionResponseDTO>> getPartnerWalletHistory(
            @Parameter(description = "ID of the partner") @PathVariable Long partnerId,
            Pageable pageable) {
        // 1. Lấy ví của partner
        WalletResponseDTO wallet = walletService.getWalletByOwner("PARTNER", partnerId);
        // 2. Lấy lịch sử từ walletId
        Page<WalletTransactionResponseDTO> history = walletService.getWalletHistoryById(wallet.getId(), pageable);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Partner funds an event", description = "Transfers coins from a partner's wallet to an event's budget.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Funding successful"),
        @ApiResponse(responseCode = "400", description = "Insufficient funds"),
        @ApiResponse(responseCode = "403", description = "Partner does not own this event"),
        @ApiResponse(responseCode = "404", description = "Partner or Event not found")
    })
    @PostMapping("/{partnerId}/fund-event")
    public ResponseEntity<EventFundingResponseDTO> fundEvent(
            @Parameter(description = "ID of the partner") @PathVariable Long partnerId,
            @Valid @RequestBody EventFundingRequestDTO requestDTO) {
        EventFundingResponseDTO fundingResponse = eventFundingService.fundEvent(partnerId, requestDTO);
        return ResponseEntity.ok(fundingResponse);
    }

    @Operation(summary = "Partner sends a broadcast to event attendees", description = "Sends a message to all students registered for a specific event.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Broadcast sent successfully"),
        @ApiResponse(responseCode = "403", description = "Partner does not own this event"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PostMapping("/{partnerId}/broadcast")
    public ResponseEntity<EventBroadcastResponseDTO> sendBroadcast(
            @Parameter(description = "ID of the partner sending") @PathVariable Long partnerId,
            @Valid @RequestBody BroadcastRequestDTO requestDTO) {
        
        EventBroadcastResponseDTO broadcastResponse = broadcastService.sendBroadcast(partnerId, requestDTO);
        return ResponseEntity.ok(broadcastResponse);
    }
}
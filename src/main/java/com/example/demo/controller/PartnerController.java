package com.example.demo.controller;

import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.dto.response.EventResponseDTO;
import com.example.demo.service.EventService;
import com.example.demo.dto.response.WalletResponseDTO;
import com.example.demo.dto.response.WalletTransactionResponseDTO;
import com.example.demo.dto.response.PageResponseDTO;
import com.example.demo.service.WalletService;
import com.example.demo.dto.request.EventFundingRequestDTO;
import com.example.demo.dto.response.EventFundingResponseDTO;
import com.example.demo.service.EventFundingService;
import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.BroadcastRequestDTO; // Thêm
import com.example.demo.dto.response.EventBroadcastResponseDTO; // Thêm
import com.example.demo.service.BroadcastService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.demo.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/partners")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "4. Partner Management")
public class PartnerController {

    private final PartnerService partnerService;
    private final EventService eventService;
    private final WalletService walletService;
    private final EventFundingService eventFundingService;
    private final BroadcastService broadcastService;

    public PartnerController(PartnerService partnerService, EventService eventService, WalletService walletService,
            EventFundingService eventFundingService, BroadcastService broadcastService) {
        this.partnerService = partnerService;
        this.eventService = eventService;
        this.walletService = walletService;
        this.eventFundingService = eventFundingService;
        this.broadcastService = broadcastService;
    }

    @Operation(summary = "Get a partner by ID", description = "Retrieves details of a specific partner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved partner"),
            @ApiResponse(responseCode = "404", description = "Partner not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PARTNERS')")
    public ResponseEntity<PartnerResponseDTO> getPartnerById(
            @Parameter(description = "ID of the partner to retrieve") @PathVariable UUID id) {
        return ResponseEntity.ok(partnerService.getPartnerById(id));
    }

    @Operation(summary = "Get Partner's organized events", description = "Returns a paginated list of all events organized by this partner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved event list"),
            @ApiResponse(responseCode = "404", description = "Partner not found")
    })
    @GetMapping("/{partnerId}/events")
    @PreAuthorize("hasRole('PARTNERS')")
    public ResponseEntity<PageResponseDTO<EventResponseDTO>> getEventsForPartner(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<EventResponseDTO> events = eventService.getEventsByPartner(principal.getPartnerId(), pageable);
        return ResponseEntity.ok(new PageResponseDTO<>(events));
    }

    @Operation(summary = "Get partner's wallet details", description = "Retrieves the wallet information for a specific partner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved wallet"),
            @ApiResponse(responseCode = "404", description = "Partner or wallet not found")
    })
    @GetMapping("/{partnerId}/wallet")
    @PreAuthorize("hasRole('PARTNERS')")
    public ResponseEntity<WalletResponseDTO> getPartnerWallet(
            @Parameter(description = "ID of the partner") @PathVariable UUID partnerId) {
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
    @PreAuthorize("hasRole('PARTNERS')")
    public ResponseEntity<PageResponseDTO<WalletTransactionResponseDTO>> getTransactionHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<WalletTransactionResponseDTO> history = walletService.getTransactionHistory(principal.getPartnerId(), "PARTNER", pageable);
        return ResponseEntity.ok(new PageResponseDTO<>(history));
    }

    @Operation(summary = "Partner funds an event", description = "Transfers coins from a partner's wallet to an event's budget.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funding successful"),
            @ApiResponse(responseCode = "400", description = "Insufficient funds"),
            @ApiResponse(responseCode = "403", description = "Partner does not own this event"),
            @ApiResponse(responseCode = "404", description = "Partner or Event not found")
    })
    @PostMapping("/{partnerId}/fund-event")
    @PreAuthorize("hasRole('PARTNERS')")
    public ResponseEntity<EventFundingResponseDTO> fundEvent(
            @Parameter(description = "ID of the partner") @PathVariable UUID partnerId,
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
    @PreAuthorize("hasRole('PARTNERS')")
    public ResponseEntity<EventBroadcastResponseDTO> sendBroadcast(
            @Parameter(description = "ID of the partner sending") @PathVariable UUID partnerId,
            @Valid @RequestBody BroadcastRequestDTO requestDTO) {

        EventBroadcastResponseDTO broadcastResponse = broadcastService.sendBroadcast(partnerId, requestDTO);
        return ResponseEntity.ok(broadcastResponse);
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
            return PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }
}
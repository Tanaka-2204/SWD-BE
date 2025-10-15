package com.example.demo.controller;

import com.example.demo.dto.request.PartnerRequestDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
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

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
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
}
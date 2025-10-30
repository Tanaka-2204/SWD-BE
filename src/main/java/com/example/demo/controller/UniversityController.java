package com.example.demo.controller;

import com.example.demo.dto.response.UniversityResponseDTO;
import com.example.demo.service.UniversityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/universities")
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @Operation(
        summary = "Get list of all universities",
        description = "Returns a list of all universities in the system. This is a public endpoint."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public ResponseEntity<List<UniversityResponseDTO>> getAllUniversities() {
        List<UniversityResponseDTO> universities = universityService.getAllUniversities();
        return ResponseEntity.ok(universities);
    }
}
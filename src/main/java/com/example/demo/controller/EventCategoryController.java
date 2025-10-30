package com.example.demo.controller;

import com.example.demo.dto.response.EventCategoryResponseDTO;
import com.example.demo.service.EventCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/event-categories")
@Tag(name = "5. Event Category", description = "APIs for managing event categories")
public class EventCategoryController {

    private final EventCategoryService eventCategoryService;

    public EventCategoryController(EventCategoryService eventCategoryService) {
        this.eventCategoryService = eventCategoryService;
    }

    @Operation(summary = "Get all event categories", description = "Returns a list of all available event categories.") 
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of categories")
    @GetMapping
    public ResponseEntity<List<EventCategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(eventCategoryService.getAllCategories());
    }

    @Operation(summary = "Get a category by ID", description = "Retrieves the details of a specific event category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the category"),
            @ApiResponse(responseCode = "404", description = "Event category with the given ID was not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventCategoryResponseDTO> getCategoryById(
            @Parameter(description = "ID of the category to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(eventCategoryService.getCategoryById(id));
    }
}
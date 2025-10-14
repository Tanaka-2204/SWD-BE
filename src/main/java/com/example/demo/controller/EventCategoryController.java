package com.example.demo.controller;

import com.example.demo.dto.request.EventCategoryRequestDTO;
import com.example.demo.dto.response.EventCategoryResponseDTO;
import com.example.demo.service.EventCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "Create a new event category", description = "Admin-only endpoint to create a new category for events.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "409", description = "Category with the same name already exists")
    })
    @PostMapping
    public ResponseEntity<EventCategoryResponseDTO> createCategory(
            @Valid @RequestBody EventCategoryRequestDTO requestDTO) {
        EventCategoryResponseDTO newCategory = eventCategoryService.createCategory(requestDTO);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all event categories", description = "Returns a list of all available event categories.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of categories")
    @GetMapping
    public ResponseEntity<List<EventCategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(eventCategoryService.getAllCategories());
    }

    @Operation(summary = "Get a category by ID", description = "Retrieves the details of a specific event category by its unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the category"),
            @ApiResponse(responseCode = "404", description = "Event category with the given ID was not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventCategoryResponseDTO> getCategoryById(
            @Parameter(description = "ID of the category to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(eventCategoryService.getCategoryById(id));
    }

    @Operation(summary = "Update an event category", description = "Updates the details of an existing event category. This is typically an admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Event category with the given ID was not found"),
            @ApiResponse(responseCode = "409", description = "Another category with the same name already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EventCategoryResponseDTO> updateCategory(
            @Parameter(description = "ID of the category to update") @PathVariable Long id,
            @Valid @RequestBody EventCategoryRequestDTO requestDTO) {
        return ResponseEntity.ok(eventCategoryService.updateCategory(id, requestDTO));
    }

    @Operation(summary = "Delete an event category", description = "Admin-only endpoint to delete a category.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID of the category to delete") @PathVariable Long id) {
        eventCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
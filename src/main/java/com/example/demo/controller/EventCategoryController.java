package com.example.demo.controller;

import com.example.demo.dto.response.EventCategoryResponseDTO;
import com.example.demo.dto.response.PageResponseDTO;
import com.example.demo.service.EventCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
@RestController
@RequestMapping("/api/v1/event-categories")
@Tag(name = "7. Public Data")
public class EventCategoryController {

    private final EventCategoryService eventCategoryService;

    public EventCategoryController(EventCategoryService eventCategoryService) {
        this.eventCategoryService = eventCategoryService;
    }

    @Operation(summary = "Get all event categories", description = "Returns a paginated list of all event categories.") 
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of categories")
    @GetMapping
    public ResponseEntity<PageResponseDTO<EventCategoryResponseDTO>> getAllCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        
        // LƯU Ý: Bạn phải cập nhật service để nhận Pageable
        Page<EventCategoryResponseDTO> categoryPage = eventCategoryService.getAllCategories(pageable);
        
        return ResponseEntity.ok(new PageResponseDTO<>(categoryPage));
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
            return PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.ASC, "id"));
        }
    }
}
package com.example.demo.controller;

import com.example.demo.dto.response.UniversityResponseDTO;
import com.example.demo.dto.response.PageResponseDTO;
import com.example.demo.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag; // <<< THÊM
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@RestController
@RequestMapping("/api/v1/universities")
@Tag(name = "7. Public Data") // <<< Gắn Tag Swagger
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    // ==========================================================
    @Operation(summary = "Get list of all universities", description = "Returns a paginated list of all universities.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public ResponseEntity<PageResponseDTO<UniversityResponseDTO>> getAllUniversities(
            @RequestParam(defaultValue = "1G") int page,
            @RequestParam(defaultValue = "20") int size, // Mặc định 20 trường
            @RequestParam(defaultValue = "name,asc") String sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        
        // LƯU Ý: Bạn phải cập nhật service để nhận Pageable
        Page<UniversityResponseDTO> universityPage = universityService.getAllUniversities(pageable);
        
        return ResponseEntity.ok(new PageResponseDTO<>(universityPage));
    }

    // ==========================================================
    // <<< HÀM HELPER ĐỂ TẠO PAGEABLE (Ẩn khỏi FE)
    // ==========================================================
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
            return PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.ASC, "name"));
        }
    }
}
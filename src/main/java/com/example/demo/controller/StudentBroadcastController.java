// TẠO TỆP MỚI: controller/StudentBroadcastController.java

package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.response.PageResponseDTO;
import com.example.demo.dto.response.StudentBroadcastResponseDTO;
import com.example.demo.service.BroadcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/broadcasts")
@Tag(name = "1. Authentication & Profile") // Gắn vào nhóm API của Sinh viên
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()") // Chỉ người đã đăng nhập mới thấy
public class StudentBroadcastController {

    private final BroadcastService broadcastService;

    public StudentBroadcastController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @Operation(summary = "Get my broadcast messages", description = "Lấy danh sách tin nhắn (phân trang) của sinh viên đã đăng nhập.")
    @GetMapping
    public ResponseEntity<PageResponseDTO<StudentBroadcastResponseDTO>> getMyBroadcasts(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @Parameter(description = "Lọc theo trạng thái (ví dụ: UNREAD)") 
            @RequestParam(required = false) String status) {
        
        Pageable pageable = createPageable(page, size, sort);
        Page<StudentBroadcastResponseDTO> broadcastPage = broadcastService.getMyBroadcasts(principal, status, pageable);
        return ResponseEntity.ok(new PageResponseDTO<>(broadcastPage));
    }

    @Operation(summary = "Get unread message count", description = "Lấy số lượng tin nhắn chưa đọc (để hiển thị badge).")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) {
        Map<String, Long> count = broadcastService.getUnreadBroadcastCount(principal);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Mark a message as read", description = "Đánh dấu một tin nhắn cụ thể là đã đọc.")
    @PatchMapping("/{deliveryId}/read")
    public ResponseEntity<StudentBroadcastResponseDTO> markAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @Parameter(description = "ID của tin nhắn (delivery ID)") 
            @PathVariable UUID deliveryId) {
        
        StudentBroadcastResponseDTO dto = broadcastService.markBroadcastAsRead(principal, deliveryId);
        return ResponseEntity.ok(dto);
    }

    // Helper tạo Pageable
    private Pageable createPageable(int page, int size, String sort) {
        int pageIndex = page > 0 ? page - 1 : 0;
        try {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")) 
                                        ? Sort.Direction.ASC 
                                        : Sort.Direction.DESC;
            return PageRequest.of(pageIndex, size, Sort.by(direction, sortField));
        } catch (Exception e) {
            return PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }
}
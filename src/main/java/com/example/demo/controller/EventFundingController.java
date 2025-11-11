package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.EventFundingRequestDTO;
import com.example.demo.dto.response.EventFundingResponseDTO;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.service.EventFundingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1") // Tiền tố chung
@Tag(name = "3. Partner Actions") // Gắn tag Swagger cho rõ ràng
public class EventFundingController {

    private final EventFundingService eventFundingService;

    public EventFundingController(EventFundingService eventFundingService) {
        this.eventFundingService = eventFundingService;
    }

    /**
     * API cho phép Partner nạp thêm ngân sách (top-up) cho một sự kiện.
     * Logic: Chuyển tiền từ Ví Partner -> Ví Event.
     * Tác dụng phụ: Tính toán lại MaxAttendees của sự kiện.
     */
    @Operation(summary = "Nạp thêm ngân sách cho sự kiện (Top-up Event Budget)",
               description = "Chuyển tiền từ Ví Partner (nguồn) sang Ví Sự kiện (đích). " +
                             "API này sẽ tính toán lại số lượng người tham dự tối đa (MaxAttendees) " +
                             "dựa trên tổng ngân sách mới.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nạp ngân sách thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (ví dụ: số tiền âm)"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Partner không sở hữu sự kiện này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Partner, Event hoặc Ví liên quan"),
            @ApiResponse(responseCode = "409", description = "Ví Partner không đủ số dư") // (Hoặc 400 tùy logic)
    })
    @PostMapping("/partners/{partnerId}/funding")
    @PreAuthorize("hasRole('PARTNERS')") // Chỉ Partner mới được gọi
    public ResponseEntity<EventFundingResponseDTO> fundEvent(
            @Parameter(description = "ID của Partner thực hiện nạp tiền") 
            @PathVariable UUID partnerId,
            
            @Valid @RequestBody EventFundingRequestDTO requestDTO,
            
            @Parameter(hidden = true) // Lấy thông tin Partner đã xác thực từ token
            @AuthenticationPrincipal AuthPrincipal principal) {

        // Kiểm tra bảo mật: Đảm bảo Partner đang đăng nhập (principal)
        // chỉ nạp tiền cho chính tài khoản của họ (partnerId)
        if (!principal.getPartnerId().equals(partnerId)) {
            throw new ForbiddenException("You can only fund events for your own partner account.");
        }

        // Gọi service (EventFundingServiceImpl) đã có
        EventFundingResponseDTO response = eventFundingService.fundEvent(partnerId, requestDTO);
        
        return ResponseEntity.ok(response);
    }
}
package com.example.demo.service;
import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.BroadcastRequestDTO;
import com.example.demo.dto.response.EventBroadcastResponseDTO;
import com.example.demo.dto.response.StudentBroadcastResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.UUID;

public interface BroadcastService {
    EventBroadcastResponseDTO sendBroadcast(UUID partnerId, BroadcastRequestDTO requestDTO);
    EventBroadcastResponseDTO sendSystemBroadcast(BroadcastRequestDTO requestDTO);
    Page<StudentBroadcastResponseDTO> getMyBroadcasts(AuthPrincipal principal, String status, Pageable pageable);

    /**
     * Đánh dấu một tin nhắn là đã đọc (READ)
     */
    StudentBroadcastResponseDTO markBroadcastAsRead(AuthPrincipal principal, UUID deliveryId);

    /**
     * Lấy số lượng tin nhắn chưa đọc
     */
    Map<String, Long> getUnreadBroadcastCount(AuthPrincipal principal);
}
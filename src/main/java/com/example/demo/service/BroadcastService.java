package com.example.demo.service;
import com.example.demo.dto.request.BroadcastRequestDTO;
import com.example.demo.dto.response.EventBroadcastResponseDTO;
import java.util.UUID;

public interface BroadcastService {
    EventBroadcastResponseDTO sendBroadcast(UUID partnerId, BroadcastRequestDTO requestDTO);
    EventBroadcastResponseDTO sendSystemBroadcast(BroadcastRequestDTO requestDTO);
}
package com.example.demo.service;
import com.example.demo.dto.request.BroadcastRequestDTO;
import com.example.demo.dto.response.EventBroadcastResponseDTO;

public interface BroadcastService {
    EventBroadcastResponseDTO sendBroadcast(Long partnerId, BroadcastRequestDTO requestDTO);
    EventBroadcastResponseDTO sendSystemBroadcast(BroadcastRequestDTO requestDTO);
}
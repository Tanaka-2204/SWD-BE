package com.example.demo.service;

import com.example.demo.dto.request.EventFundingRequestDTO;
import com.example.demo.dto.response.EventFundingResponseDTO;
import java.util.UUID;

public interface EventFundingService {
    
    EventFundingResponseDTO fundEvent(UUID partnerId, EventFundingRequestDTO requestDTO);
}
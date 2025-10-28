package com.example.demo.service;

import com.example.demo.dto.request.EventFundingRequestDTO;
import com.example.demo.dto.response.EventFundingResponseDTO;

public interface EventFundingService {
    
    EventFundingResponseDTO fundEvent(Long partnerId, EventFundingRequestDTO requestDTO);
}
package com.example.demo.service;

import com.example.demo.dto.request.AIHelpRequestDTO;
import java.util.Map;

public interface AIContextService {
    Map<String, Object> getStaticContext();
    Map<String, Object> getHelpAnswer(AIHelpRequestDTO request);
}
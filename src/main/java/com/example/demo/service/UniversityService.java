package com.example.demo.service;

import com.example.demo.dto.response.UniversityResponseDTO;
import java.util.List;

public interface UniversityService {
    List<UniversityResponseDTO> getAllUniversities();
}
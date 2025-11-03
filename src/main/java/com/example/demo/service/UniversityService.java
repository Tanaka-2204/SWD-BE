package com.example.demo.service;

import com.example.demo.dto.request.UniversityRequestDTO;
import com.example.demo.dto.response.UniversityResponseDTO;
import org.springframework.data.domain.Page; // <<< THÊM
import org.springframework.data.domain.Pageable;

public interface UniversityService {
    Page<UniversityResponseDTO> getAllUniversities(Pageable pageable);
    UniversityResponseDTO createUniversity(UniversityRequestDTO dto);
    UniversityResponseDTO updateUniversity(Long universityId, UniversityRequestDTO dto);
    void deleteUniversity(Long universityId);
}
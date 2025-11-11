package com.example.demo.service;

import com.example.demo.dto.request.UniversityRequestDTO;
import com.example.demo.dto.response.UniversityResponseDTO;
import org.springframework.data.domain.Page; // <<< THÊM
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface UniversityService {
    Page<UniversityResponseDTO> getAllUniversities(Pageable pageable);
    UniversityResponseDTO createUniversity(UniversityRequestDTO dto);
    UniversityResponseDTO updateUniversity(UUID universityId, UniversityRequestDTO dto);
    void deleteUniversity(UUID universityId);
}
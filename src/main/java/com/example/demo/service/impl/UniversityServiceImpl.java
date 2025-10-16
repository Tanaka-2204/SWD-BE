package com.example.demo.service.impl;

import com.example.demo.dto.response.UniversityResponseDTO;
import com.example.demo.entity.University;
import com.example.demo.repository.UniversityRepository;
import com.example.demo.service.UniversityService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;

    public UniversityServiceImpl(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    @Override
    public List<UniversityResponseDTO> getAllUniversities() {
        return universityRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UniversityResponseDTO convertToDTO(University university) {
        UniversityResponseDTO dto = new UniversityResponseDTO();
        dto.setId(university.getId());
        dto.setName(university.getName());
        dto.setCode(university.getCode());
        dto.setDomain(university.getDomain());
        dto.setCreatedAt(university.getCreatedAt());
        return dto;
    }
}
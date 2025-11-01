package com.example.demo.service.impl;

import com.example.demo.dto.request.UniversityRequestDTO; // <<< THÊM
import com.example.demo.dto.response.UniversityResponseDTO;
import com.example.demo.entity.University;
import com.example.demo.exception.DataIntegrityViolationException; // <<< THÊM
import com.example.demo.exception.ResourceNotFoundException; // <<< THÊM
import com.example.demo.repository.StudentRepository; // <<< THÊM
import com.example.demo.repository.UniversityRepository;
import com.example.demo.service.UniversityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <<< THÊM
import org.springframework.data.domain.Page; // <<< THÊM
import org.springframework.data.domain.Pageable;

@Service
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;
    private final StudentRepository studentRepository; // <<< THÊM

    // <<< SỬA CONSTRUCTOR >>>
    public UniversityServiceImpl(UniversityRepository universityRepository, StudentRepository studentRepository) {
        this.universityRepository = universityRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UniversityResponseDTO> getAllUniversities(Pageable pageable) {
        // (Giả sử bạn có hàm 'toUniversityDTO')
        return universityRepository.findAll(pageable)
                                   .map(this::convertToDTO);
    }

    // <<< THÊM PHƯƠNG THỨC TẠO MỚI >>>
    @Override
    @Transactional
    public UniversityResponseDTO createUniversity(UniversityRequestDTO dto) {
        // Kiểm tra trùng lặp
        if (universityRepository.existsByCode(dto.getCode())) {
            throw new DataIntegrityViolationException("University code '" + dto.getCode() + "' already exists.");
        }
        universityRepository.findByName(dto.getName()).ifPresent(u -> {
            throw new DataIntegrityViolationException("University name '" + dto.getName() + "' already exists.");
        });

        University university = new University();
        university.setName(dto.getName());
        university.setCode(dto.getCode().toUpperCase());
        university.setDomain(dto.getDomain());

        University savedUniversity = universityRepository.save(university);
        return convertToDTO(savedUniversity);
    }

    // <<< THÊM PHƯƠNG THỨC CẬP NHẬT >>>
    @Override
    @Transactional
    public UniversityResponseDTO updateUniversity(Long universityId, UniversityRequestDTO dto) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("University not found with id: " + universityId));

        // Kiểm tra trùng lặp (ngoại trừ chính nó)
        universityRepository.findByCode(dto.getCode()).ifPresent(u -> {
            if (!u.getId().equals(universityId)) {
                throw new DataIntegrityViolationException("University code '" + dto.getCode() + "' already exists.");
            }
        });
        universityRepository.findByName(dto.getName()).ifPresent(u -> {
            if (!u.getId().equals(universityId)) {
                throw new DataIntegrityViolationException("University name '" + dto.getName() + "' already exists.");
            }
        });

        university.setName(dto.getName());
        university.setCode(dto.getCode().toUpperCase());
        university.setDomain(dto.getDomain());

        University updatedUniversity = universityRepository.save(university);
        return convertToDTO(updatedUniversity);
    }

    // <<< THÊM PHƯƠNG THỨC XÓA >>>
    @Override
    @Transactional
    public void deleteUniversity(Long universityId) {
        if (!universityRepository.existsById(universityId)) {
            throw new ResourceNotFoundException("University not found with id: " + universityId);
        }

        // Kiểm tra xem có sinh viên nào đang dùng trường này không
        if (studentRepository.existsByUniversityId(universityId)) {
            throw new DataIntegrityViolationException(
                "Cannot delete university. It is currently associated with one or more students."
            );
        }

        universityRepository.deleteById(universityId);
    }

    // <<< THÊM HELPER CHUYỂN ĐỔI DTO >>>
    private UniversityResponseDTO convertToDTO(University university) {
        UniversityResponseDTO dto = new UniversityResponseDTO();
        dto.setId(university.getId());
        dto.setName(university.getName());
        dto.setCode(university.getCode());
        dto.setDomain(university.getDomain());
        return dto;
    }
}
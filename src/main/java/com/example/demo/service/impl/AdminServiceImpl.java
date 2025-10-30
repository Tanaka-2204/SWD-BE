package com.example.demo.service.impl;

import com.example.demo.repository.AdminRepository;
import com.example.demo.service.AdminService;
import com.example.demo.dto.response.AdminResponseDTO;
import com.example.demo.entity.Admin;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository; // <<< Sửa tên (nếu cần)

    public AdminServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponseDTO getAdminByCognitoSub(String cognitoSub) {
        Admin admin = adminRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found for authenticated user."));
        return convertToDTO(admin);
    }

    // <<< THÊM HELPER CHUYỂN ĐỔI >>>
    private AdminResponseDTO convertToDTO(Admin admin) {
        AdminResponseDTO dto = new AdminResponseDTO();
        dto.setId(admin.getId());
        dto.setEmail(admin.getEmail());
        dto.setFullName(admin.getFullName());
        dto.setCognitoSub(admin.getCognitoSub());
        dto.setCreatedAt(admin.getCreatedAt());
        return dto;
    }
}
package com.example.demo.service.impl;

import com.example.demo.dto.response.AdminResponseDTO;
import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;
import com.example.demo.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminUserRepository;

    // Sửa constructor để nhận AdminUserRepository
    public AdminServiceImpl(AdminRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

}
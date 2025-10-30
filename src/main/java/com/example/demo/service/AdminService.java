package com.example.demo.service;

import com.example.demo.dto.response.AdminResponseDTO;


public interface AdminService {
    AdminResponseDTO getAdminByCognitoSub(String cognitoSub);
}

package com.example.demo.service;

import com.example.demo.dto.request.TestLoginRequestDTO;
import com.example.demo.dto.response.TestLoginResponseDTO;

public interface TestLoginService {
    TestLoginResponseDTO loginForTest(TestLoginRequestDTO requestDTO);
}
package com.example.demo.service;

import com.example.demo.dto.request.StudentProfileCompletionDTO;
import com.example.demo.dto.request.StudentProfileUpdateDTO;
import com.example.demo.dto.response.StudentResponseDTO;


public interface StudentService {

    StudentResponseDTO getStudentById(Long studentId);

    StudentResponseDTO completeProfile(String cognitoSub, String email, StudentProfileCompletionDTO dto);

    StudentResponseDTO updateMyProfile(String cognitoSub, StudentProfileUpdateDTO updateDTO);
}
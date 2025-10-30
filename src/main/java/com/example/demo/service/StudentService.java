package com.example.demo.service;

import com.example.demo.dto.request.StudentProfileCompletionDTO;
import com.example.demo.dto.request.StudentProfileUpdateDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface StudentService {

    StudentResponseDTO getStudentById(Long studentId);

    StudentResponseDTO completeProfile(String cognitoSub, String email, StudentProfileCompletionDTO dto);

    StudentResponseDTO updateMyProfile(String cognitoSub, StudentProfileUpdateDTO updateDTO);

    Page<StudentResponseDTO> getAllStudents(Pageable pageable);

    StudentResponseDTO updateStudentStatus(Long studentId, UserStatusUpdateDTO dto);
}
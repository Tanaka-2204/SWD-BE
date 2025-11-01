package com.example.demo.service;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.StudentProfileCompletionDTO;
import com.example.demo.dto.request.StudentProfileUpdateDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.entity.Student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface StudentService {

    StudentResponseDTO getStudentById(Long studentId);

    StudentResponseDTO completeProfile(AuthPrincipal principal, 
                                     String rawAccessToken, // <<< THÊM THAM SỐ NÀY
                                     StudentProfileCompletionDTO completionDTO);

    StudentResponseDTO updateMyProfile(String cognitoSub, StudentProfileUpdateDTO updateDTO);

    Page<StudentResponseDTO> getAllStudents(Pageable pageable);

    StudentResponseDTO updateStudentStatus(Long studentId, UserStatusUpdateDTO dto);
    
    StudentResponseDTO toResponseDTO(Student student);
}
package com.example.demo.service;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.StudentProfileCompletionDTO;
import com.example.demo.dto.request.StudentProfileUpdateDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.entity.Student;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;


public interface StudentService {

    StudentResponseDTO getStudentById(UUID studentId);

    StudentResponseDTO completeProfile(AuthPrincipal principal, 
                                     String rawAccessToken, 
                                     StudentProfileCompletionDTO completionDTO,
                                     MultipartFile avatarFile);

    StudentResponseDTO updateMyProfile(String cognitoSub, 
                                     StudentProfileUpdateDTO updateDTO,
                                     MultipartFile avatarFile);

    Page<StudentResponseDTO> getAllStudents(Pageable pageable);

    StudentResponseDTO updateStudentStatus(UUID studentId, UserStatusUpdateDTO dto);
    
    StudentResponseDTO toResponseDTO(Student student);
}
package com.example.demo.service;

import com.example.demo.dto.StudentProfileUpdateRequest;
import com.example.demo.entity.Student;
import com.example.demo.exception.ResourceNotFoundException; 
import com.example.demo.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Student updateStudentProfile(String cognitoSub, StudentProfileUpdateRequest request) {
        logger.info("Attempting to update profile for cognitoSub: {}", cognitoSub);

        Student student = studentRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> {
                    logger.warn("Student not found with cognitoSub: {}", cognitoSub);
                    return new ResourceNotFoundException("Student not found with cognitoSub: " + cognitoSub);
                });

        student.setName(request.getName());
        student.setAvatarUrl(request.getAvatarUrl());
        student.setInterests(request.getInterests());

        Student updatedStudent = studentRepository.save(student);
        logger.info("Successfully updated profile for studentId: {}", updatedStudent.getStudentId());
        
        return updatedStudent;
    }
}
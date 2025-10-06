package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Student updateStudentProfile(String cognitoSub, StudentProfileUpdateRequest request) {
        // Tìm sinh viên bằng cognitoSub, nếu không thấy sẽ báo lỗi
        Student student = studentRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> new RuntimeException("Student not found with cognitoSub: " + cognitoSub));

        // Cập nhật các trường được phép
        student.setName(request.getName());
        student.setAvatarUrl(request.getAvatarUrl());
        student.setInterests(request.getInterests());

        // Lưu lại vào database
        return studentRepository.save(student);
    }
}

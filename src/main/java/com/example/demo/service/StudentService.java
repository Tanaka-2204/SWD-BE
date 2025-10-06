package com.example.demo.service;

import com.example.demo.dto.StudentDTO;
import com.example.demo.dto.StudentRequest;
import com.example.demo.entity.Student;
import com.example.demo.entity.University;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UniversityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UniversityRepository universityRepository;

    public StudentService(StudentRepository studentRepository, UniversityRepository universityRepository) {
        this.studentRepository = studentRepository;
        this.universityRepository = universityRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        return convertToDTO(student);
    }

    @Transactional
    public StudentDTO createStudent(StudentRequest request) {
        University university = universityRepository.findById(request.getUniversityId())
                .orElseThrow(() -> new RuntimeException("University not found with id: " + request.getUniversityId()));

        Student student = new Student();
        student.setUniversity(university);
        student.setFullName(request.getFullName());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setEmail(request.getEmail());
        student.setAvatarUrl(request.getAvatarUrl());

        Student saved = studentRepository.save(student);
        return convertToDTO(saved);
    }

    @Transactional
    public StudentDTO updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        if (request.getUniversityId() != null) {
            University university = universityRepository.findById(request.getUniversityId())
                    .orElseThrow(() -> new RuntimeException("University not found with id: " + request.getUniversityId()));
            student.setUniversity(university);
        }

        if (request.getFullName() != null) {
            student.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            student.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            student.setEmail(request.getEmail());
        }
        if (request.getAvatarUrl() != null) {
            student.setAvatarUrl(request.getAvatarUrl());
        }

        Student updated = studentRepository.save(student);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setUniversityId(student.getUniversity().getId());
        dto.setUniversityName(student.getUniversity().getName());
        dto.setFullName(student.getFullName());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setEmail(student.getEmail());
        dto.setAvatarUrl(student.getAvatarUrl());
        dto.setCreatedAt(student.getCreatedAt());
        return dto;
    }
}

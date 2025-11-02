package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByPhoneNumber(String phoneNumber);
    Optional<Student> findByEmail(String email);
    boolean existsByUniversityId(Long universityId);
    Optional<Student> findByCognitoSub(String cognitoSub);
    // Custom upsert method can be defined here if needed
}
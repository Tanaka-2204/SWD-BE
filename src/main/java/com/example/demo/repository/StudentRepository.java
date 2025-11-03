package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByPhoneNumber(String phoneNumber);
    Optional<Student> findByEmail(String email);
    boolean existsByUniversityId(Long universityId);
    Optional<Student> findByCognitoSub(String cognitoSub);

    // THÊM method này để fetch wallet cùng lúc
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.wallet WHERE s.cognitoSub = :cognitoSub")
    Optional<Student> findByCognitoSubWithWallet(@Param("cognitoSub") String cognitoSub);
}
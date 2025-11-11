package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByPhoneNumber(String phoneNumber);
    Optional<Student> findByEmail(String email);
    boolean existsByUniversityId(UUID universityId);
    Optional<Student> findByCognitoSub(String cognitoSub);

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.wallet WHERE s.cognitoSub = :cognitoSub")
    Optional<Student> findByCognitoSubWithWallet(@Param("cognitoSub") String cognitoSub);
}
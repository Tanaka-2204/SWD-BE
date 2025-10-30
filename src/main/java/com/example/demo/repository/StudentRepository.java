package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByPhoneNumber(String phoneNumber);
    Optional<Student> findByEmail(String email);
    boolean existsByUniversityId(Long universityId);
    Optional<Student> findByCognitoSub(String cognitoSub);
    // Custom upsert method can be defined here if needed
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO student (cognito_sub, email, name, point_balance)
            VALUES (:cognitoSub, :email, :name, 0)
            ON CONFLICT (cognito_sub) DO UPDATE
            SET email = EXCLUDED.email,
                name = EXCLUDED.name
            """, nativeQuery = true)
    void upsertStudent(@Param("cognitoSub") String cognitoSub,
                       @Param("email") String email,
                       @Param("name") String name);
}
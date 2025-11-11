package com.example.demo.repository;

import com.example.demo.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID> {

    Optional<Partner> findByName(String name);
    Optional<Partner> findByCognitoSub(String cognitoSub);
}
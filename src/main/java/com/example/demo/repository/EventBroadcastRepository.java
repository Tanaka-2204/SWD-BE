package com.example.demo.repository;

import com.example.demo.entity.EventBroadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EventBroadcastRepository extends JpaRepository<EventBroadcast, UUID> {
}
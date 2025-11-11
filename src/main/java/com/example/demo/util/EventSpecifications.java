package com.example.demo.util;

import com.example.demo.entity.Event;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventSpecifications {

    public static Specification<Event> filterBy(UUID categoryId, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (status != null && !status.isBlank()) {
                // Có thể cần chuẩn hóa status (toUpperCase, etc.)
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("status")), status.toUpperCase()));
            }
            
            // Chỉ lấy các sự kiện không phải DRAFT hoặc CANCELLED cho public view? (Tùy chọn)
            // predicates.add(criteriaBuilder.notEqual(root.get("status"), "DRAFT"));
            // predicates.add(criteriaBuilder.notEqual(root.get("status"), "CANCELLED"));


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
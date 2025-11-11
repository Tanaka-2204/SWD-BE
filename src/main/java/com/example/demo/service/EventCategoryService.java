package com.example.demo.service;

import com.example.demo.dto.request.EventCategoryRequestDTO;
import com.example.demo.dto.response.EventCategoryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface EventCategoryService {

    EventCategoryResponseDTO createCategory(EventCategoryRequestDTO requestDTO);

    EventCategoryResponseDTO getCategoryById(UUID categoryId);

    Page<EventCategoryResponseDTO> getAllCategories(Pageable pageable);

    EventCategoryResponseDTO updateCategory(UUID categoryId, EventCategoryRequestDTO requestDTO);

    void deleteCategory(UUID categoryId);
}
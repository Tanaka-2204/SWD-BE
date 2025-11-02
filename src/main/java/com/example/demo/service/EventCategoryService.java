package com.example.demo.service;

import com.example.demo.dto.request.EventCategoryRequestDTO;
import com.example.demo.dto.response.EventCategoryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventCategoryService {

    EventCategoryResponseDTO createCategory(EventCategoryRequestDTO requestDTO);

    EventCategoryResponseDTO getCategoryById(Long categoryId);

    Page<EventCategoryResponseDTO> getAllCategories(Pageable pageable);

    EventCategoryResponseDTO updateCategory(Long categoryId, EventCategoryRequestDTO requestDTO);

    void deleteCategory(Long categoryId);
}
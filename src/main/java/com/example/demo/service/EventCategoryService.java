package com.example.demo.service;

import com.example.demo.dto.request.EventCategoryRequestDTO;
import com.example.demo.dto.response.EventCategoryResponseDTO;
import java.util.List;

public interface EventCategoryService {

    EventCategoryResponseDTO createCategory(EventCategoryRequestDTO requestDTO);

    EventCategoryResponseDTO getCategoryById(Long categoryId);

    List<EventCategoryResponseDTO> getAllCategories();

    EventCategoryResponseDTO updateCategory(Long categoryId, EventCategoryRequestDTO requestDTO);

    void deleteCategory(Long categoryId);
}
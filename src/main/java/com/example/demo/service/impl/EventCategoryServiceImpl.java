package com.example.demo.service.impl;

import com.example.demo.dto.request.EventCategoryRequestDTO;
import com.example.demo.dto.response.EventCategoryResponseDTO;
import com.example.demo.entity.EventCategory;
import com.example.demo.exception.DataIntegrityViolationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.EventCategoryRepository;
import com.example.demo.service.EventCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventCategoryServiceImpl implements EventCategoryService {

    private final EventCategoryRepository eventCategoryRepository;

    public EventCategoryServiceImpl(EventCategoryRepository eventCategoryRepository) {
        this.eventCategoryRepository = eventCategoryRepository;
    }

    @Override
    @Transactional
    public EventCategoryResponseDTO createCategory(EventCategoryRequestDTO requestDTO) {
        // 1. Kiểm tra tên danh mục đã tồn tại chưa
        eventCategoryRepository.findByName(requestDTO.getName()).ifPresent(c -> {
            throw new DataIntegrityViolationException("Event category with name '" + requestDTO.getName() + "' already exists.");
        });

        // 2. Chuyển đổi từ DTO sang Entity và lưu
        EventCategory category = new EventCategory();
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());

        EventCategory savedCategory = eventCategoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public EventCategoryResponseDTO getCategoryById(Long categoryId) {
        EventCategory category = eventCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Event category not found with id: " + categoryId));
        return convertToDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventCategoryResponseDTO> getAllCategories() {
        return eventCategoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventCategoryResponseDTO updateCategory(Long categoryId, EventCategoryRequestDTO requestDTO) {
        // 1. Tìm category cần cập nhật
        EventCategory category = eventCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Event category not found with id: " + categoryId));

        // 2. Kiểm tra nếu tên mới được cung cấp và tên đó đã tồn tại (và không phải của chính category này)
        if (requestDTO.getName() != null && !requestDTO.getName().equals(category.getName())) {
            eventCategoryRepository.findByName(requestDTO.getName()).ifPresent(c -> {
                throw new DataIntegrityViolationException("Event category with name '" + requestDTO.getName() + "' already exists.");
            });
            category.setName(requestDTO.getName());
        }
        
        // 3. Cập nhật các trường khác
        if (requestDTO.getDescription() != null) {
            category.setDescription(requestDTO.getDescription());
        }

        EventCategory updatedCategory = eventCategoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (!eventCategoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Event category not found with id: " + categoryId);
        }
        // Lưu ý: Cần xử lý logic nếu category này đang được sử dụng bởi các Event.
        // Ví dụ: không cho xóa hoặc gán các event đó về category "Uncategorized".
        eventCategoryRepository.deleteById(categoryId);
    }

    // Helper method để chuyển đổi Entity sang DTO
    private EventCategoryResponseDTO convertToDTO(EventCategory category) {
        EventCategoryResponseDTO dto = new EventCategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
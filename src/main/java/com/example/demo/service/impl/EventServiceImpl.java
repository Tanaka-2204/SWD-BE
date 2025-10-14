package com.example.demo.service.impl;

import com.example.demo.dto.request.EventCreateDTO;
import com.example.demo.dto.request.EventUpdateDTO;
import com.example.demo.dto.response.EventCategoryResponseDTO;
import com.example.demo.dto.response.EventResponseDTO;
import com.example.demo.entity.Event;
import com.example.demo.entity.EventCategory;
import com.example.demo.entity.Partner;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.EventCategoryRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.PartnerRepository;
import com.example.demo.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final PartnerRepository partnerRepository;
    private final EventCategoryRepository categoryRepository;

    public EventServiceImpl(EventRepository eventRepository,
                            PartnerRepository partnerRepository,
                            EventCategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.partnerRepository = partnerRepository;
        this.categoryRepository = categoryRepository;
    }

    // --- CREATE ---
    @Override
    @Transactional
    public EventResponseDTO createEvent(Long partnerId, EventCreateDTO requestDTO) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));
        
        EventCategory category = null;
        if (requestDTO.getCategoryId() != null) {
            category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventCategory not found with id: " + requestDTO.getCategoryId()));
        }

        Event event = new Event();
        event.setPartner(partner);
        event.setCategory(category);
        event.setTitle(requestDTO.getTitle());
        event.setDescription(requestDTO.getDescription());
        event.setStartTime(requestDTO.getStartTime());
        event.setEndTime(requestDTO.getEndTime());
        event.setLocation(requestDTO.getLocation());
        event.setRewardPerCheckin(requestDTO.getRewardPerCheckin());
        event.setTotalBudgetCoin(requestDTO.getTotalBudgetCoin());
        event.setStatus("DRAFT");

        return convertToDTO(eventRepository.save(event));
    }

    // --- READ ---
    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        return convertToDTO(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> getAllEvents(Pageable pageable) {
        Page<Event> eventPage = eventRepository.findAll(pageable);
        return eventPage.map(this::convertToDTO);
    }

    // --- UPDATE ---
    @Override
    @Transactional
    public EventResponseDTO updateEvent(Long eventId, EventUpdateDTO requestDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        
        // Cập nhật các trường nếu chúng được cung cấp trong DTO
        if (requestDTO.getTitle() != null) event.setTitle(requestDTO.getTitle());
        if (requestDTO.getDescription() != null) event.setDescription(requestDTO.getDescription());
        if (requestDTO.getStartTime() != null) event.setStartTime(requestDTO.getStartTime());
        if (requestDTO.getEndTime() != null) event.setEndTime(requestDTO.getEndTime());
        if (requestDTO.getLocation() != null) event.setLocation(requestDTO.getLocation());
        if (requestDTO.getRewardPerCheckin() != null) event.setRewardPerCheckin(requestDTO.getRewardPerCheckin());
        if (requestDTO.getStatus() != null) event.setStatus(requestDTO.getStatus());

        if (requestDTO.getCategoryId() != null) {
            EventCategory category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("EventCategory not found with id: " + requestDTO.getCategoryId()));
            event.setCategory(category);
        }
        
        return convertToDTO(eventRepository.save(event));
    }

    // --- DELETE ---
    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        // Lưu ý: Cần logic kiểm tra xem sự kiện có đang hoạt động hoặc có người đăng ký không trước khi xóa.
        eventRepository.deleteById(eventId);
    }
    
    // --- BUSINESS LOGIC IMPLEMENTATIONS ---
    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> getEventsByPartner(Long partnerId, Pageable pageable) {
        if (!partnerRepository.existsById(partnerId)) {
            throw new ResourceNotFoundException("Partner not found with id: " + partnerId);
        }
        return eventRepository.findAllByPartnerId(partnerId, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("EventCategory not found with id: " + categoryId);
        }
        return eventRepository.findAllByCategoryId(categoryId).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findAllByStartTimeAfter(OffsetDateTime.now(), pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getOngoingEvents() {
        return eventRepository.findOngoingEvents(OffsetDateTime.now()).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> searchEventsByTitle(String keyword, Pageable pageable) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword, pageable).map(this::convertToDTO);
    }
    
    // --- HELPER METHOD ---
    private EventResponseDTO convertToDTO(Event event) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setLocation(event.getLocation());
        dto.setStatus(event.getStatus());
        dto.setRewardPerCheckin(event.getRewardPerCheckin());
        dto.setTotalBudgetCoin(event.getTotalBudgetCoin());
        dto.setCreatedAt(event.getCreatedAt());

        if (event.getPartner() != null) {
            dto.setPartnerId(event.getPartner().getId());
            dto.setPartnerName(event.getPartner().getName());
        }

        if (event.getCategory() != null) {
            EventCategoryResponseDTO categoryDTO = new EventCategoryResponseDTO();
            categoryDTO.setId(event.getCategory().getId());
            categoryDTO.setName(event.getCategory().getName());
            categoryDTO.setDescription(event.getCategory().getDescription());
            dto.setCategory(categoryDTO);
        }
        return dto;
    }
}
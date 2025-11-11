package com.example.demo.service;

import com.example.demo.dto.request.PartnerRequestDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.exception.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface PartnerService {

    PartnerResponseDTO createPartner(PartnerRequestDTO requestDTO) throws BadRequestException;

    PartnerResponseDTO getPartnerById(UUID partnerId);

    PartnerResponseDTO getPartnerByCognitoSub(String cognitoSub);

    Page<PartnerResponseDTO> getAllPartners(Pageable pageable);

    PartnerResponseDTO updatePartner(UUID partnerId, PartnerRequestDTO requestDTO);

    PartnerResponseDTO updatePartnerStatus(UUID partnerId, UserStatusUpdateDTO dto);

    void deletePartner(UUID partnerId);
}
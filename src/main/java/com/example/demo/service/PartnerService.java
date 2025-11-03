package com.example.demo.service;

import com.example.demo.dto.request.PartnerRequestDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.exception.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartnerService {

    PartnerResponseDTO createPartner(PartnerRequestDTO requestDTO) throws BadRequestException;

    PartnerResponseDTO getPartnerById(Long partnerId);

    PartnerResponseDTO getPartnerByCognitoSub(String cognitoSub);

    Page<PartnerResponseDTO> getAllPartners(Pageable pageable);

    PartnerResponseDTO updatePartner(Long partnerId, PartnerRequestDTO requestDTO);

    PartnerResponseDTO updatePartnerStatus(Long partnerId, UserStatusUpdateDTO dto);

    void deletePartner(Long partnerId);
}
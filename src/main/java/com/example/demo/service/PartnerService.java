package com.example.demo.service;

import com.example.demo.dto.request.PartnerRequestDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.exception.BadRequestException;
import java.util.List;

public interface PartnerService {

    PartnerResponseDTO createPartner(PartnerRequestDTO requestDTO) throws BadRequestException;

    PartnerResponseDTO getPartnerById(Long partnerId);

    PartnerResponseDTO getPartnerByCognitoSub(String cognitoSub);

    List<PartnerResponseDTO> getAllPartners();

    PartnerResponseDTO updatePartner(Long partnerId, PartnerRequestDTO requestDTO);

    PartnerResponseDTO updatePartnerStatus(Long partnerId, UserStatusUpdateDTO dto);

    void deletePartner(Long partnerId);
}
package com.example.demo.service.impl;

import com.example.demo.dto.request.PartnerRequestDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.entity.Partner;
import com.example.demo.entity.Wallet;
import com.example.demo.exception.DataIntegrityViolationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PartnerRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.service.PartnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final WalletRepository walletRepository;

    public PartnerServiceImpl(PartnerRepository partnerRepository, WalletRepository walletRepository) {
        this.partnerRepository = partnerRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional // Rất quan trọng: Đảm bảo tất cả thao tác thành công hoặc không gì cả
    public PartnerResponseDTO createPartner(PartnerRequestDTO requestDTO) {
        // 1. Kiểm tra xem tên đối tác đã tồn tại chưa
        partnerRepository.findByName(requestDTO.getName()).ifPresent(p -> {
            throw new DataIntegrityViolationException("Partner with name '" + requestDTO.getName() + "' already exists.");
        });

        // 2. Tạo đối tượng Partner từ DTO
        Partner partner = new Partner();
        partner.setName(requestDTO.getName());
        partner.setOrganizationType(requestDTO.getOrganizationType());
        partner.setContactEmail(requestDTO.getContactEmail());
        partner.setContactPhone(requestDTO.getContactPhone());

        // 3. Tự động tạo một ví mới cho đối tác
        Wallet wallet = new Wallet();
        wallet.setOwnerType("PARTNER"); // Đánh dấu chủ sở hữu là Partner
        Wallet savedWallet = walletRepository.save(wallet);

        // 4. Gán ví vừa tạo cho đối tác
        partner.setWallet(savedWallet);
        Partner savedPartner = partnerRepository.save(partner);

        // 5. Cập nhật lại ID chủ sở hữu cho ví và lưu lại
        savedWallet.setOwnerId(savedPartner.getId());
        walletRepository.save(savedWallet);

        return convertToDTO(savedPartner);
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerResponseDTO getPartnerById(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));
        return convertToDTO(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartnerResponseDTO> getAllPartners() {
        return partnerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PartnerResponseDTO updatePartner(Long partnerId, PartnerRequestDTO requestDTO) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));

        partner.setName(requestDTO.getName());
        partner.setOrganizationType(requestDTO.getOrganizationType());
        partner.setContactEmail(requestDTO.getContactEmail());
        partner.setContactPhone(requestDTO.getContactPhone());

        Partner updatedPartner = partnerRepository.save(partner);
        return convertToDTO(updatedPartner);
    }

    @Override
    @Transactional
    public void deletePartner(Long partnerId) {
        if (!partnerRepository.existsById(partnerId)) {
            throw new ResourceNotFoundException("Partner not found with id: " + partnerId);
        }
        // Lưu ý: Logic này chưa xử lý việc xóa các sự kiện hoặc giao dịch liên quan.
        // Cần cân nhắc kỹ trước khi xóa partner.
        partnerRepository.deleteById(partnerId);
    }

    // Helper method to convert Partner Entity to DTO
    private PartnerResponseDTO convertToDTO(Partner partner) {
        PartnerResponseDTO dto = new PartnerResponseDTO();
        dto.setId(partner.getId());
        dto.setName(partner.getName());
        dto.setOrganizationType(partner.getOrganizationType());
        dto.setContactEmail(partner.getContactEmail());
        dto.setContactPhone(partner.getContactPhone());
        dto.setCreatedAt(partner.getCreatedAt());
        if (partner.getWallet() != null) {
            dto.setWalletId(partner.getWallet().getId());
        }
        return dto;
    }
}
package com.example.demo.service.impl;

import com.example.demo.dto.response.WalletResponseDTO;
import com.example.demo.entity.Student;
import com.example.demo.entity.Wallet;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.RedemptionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RedemptionServiceImpl implements RedemptionService {

    private static final Logger log = LoggerFactory.getLogger(RedemptionServiceImpl.class);

    private final StudentRepository studentRepository;

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDTO getStudentWalletByCognitoSub(String cognitoSub) {
        // Dùng method mới để fetch wallet cùng lúc
        Student student = studentRepository.findByCognitoSubWithWallet(cognitoSub)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Wallet wallet = student.getWallet(); // Bây giờ không bị LazyInitializationException
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for student");
        }

        log.info("Wallet for student {} -> balance={}", student.getId(), wallet.getBalance());

        WalletResponseDTO dto = new WalletResponseDTO();
        dto.setId(wallet.getId());
        dto.setOwnerType(wallet.getOwnerType());
        dto.setOwnerId(wallet.getOwnerId());
        dto.setCurrency(wallet.getCurrency());
        dto.setBalance(wallet.getBalance());
        dto.setCreatedAt(wallet.getCreatedAt());
        return dto;
    }
}

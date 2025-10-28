package com.example.demo.service.impl;

import com.example.demo.dto.request.CheckinRequestDTO;
import com.example.demo.dto.response.CheckinResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import com.example.demo.service.CheckinService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class CheckinServiceImpl implements CheckinService {

    private final EventRepository eventRepository;
    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;
    private final CheckinRepository checkinRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public CheckinServiceImpl(EventRepository eventRepository, StudentRepository studentRepository, RegistrationRepository registrationRepository, CheckinRepository checkinRepository, WalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.eventRepository = eventRepository;
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
        this.checkinRepository = checkinRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public CheckinResponseDTO performCheckin(Long eventId, CheckinRequestDTO requestDTO) {
        // 1. Tìm sự kiện
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        
        // 2. Tìm sinh viên bằng SĐT
        Student student = studentRepository.findByPhoneNumber(requestDTO.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with phone: " + requestDTO.getPhoneNumber()));
        
        // 3. Kiểm tra xem sinh viên đã đăng ký sự kiện này chưa
        registrationRepository.findByStudentIdAndEventId(student.getId(), eventId)
                .orElseThrow(() -> new DataIntegrityViolationException("Student is not registered for this event."));
        
        // 4. Kiểm tra xem đã check-in chưa
        checkinRepository.findByEventIdAndStudentId(eventId, student.getId()).ifPresent(c -> {
            throw new DataIntegrityViolationException("Student has already checked in.");
        });

        // 5. Tạo bản ghi Check-in
        Checkin checkin = new Checkin();
        checkin.setEvent(event);
        checkin.setStudent(student); // Giả sử bạn thêm cột student_id vào bảng checkin
        checkin.setPhoneNumber(requestDTO.getPhoneNumber()); // Hoặc giữ nguyên SĐT
        checkin.setCheckinTime(OffsetDateTime.now());
        
        // 6. Xử lý logic Thưởng (REWARD)
        BigDecimal rewardAmount = event.getRewardPerCheckin();
        boolean rewardGranted = false;
        
        if (rewardAmount != null && rewardAmount.compareTo(BigDecimal.ZERO) > 0) {
            // Lấy ví sự kiện (từ partner) và ví sinh viên
            Wallet partnerWallet = event.getPartner().getWallet();
            Wallet studentWallet = walletRepository.findByOwnerTypeAndOwnerId("STUDENT", student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student wallet not found."));

            // Kiểm tra ngân sách sự kiện (hoặc ngân sách partner)
            if (partnerWallet.getBalance().compareTo(rewardAmount) >= 0) {
                // Trừ tiền Partner
                partnerWallet.setBalance(partnerWallet.getBalance().subtract(rewardAmount));
                walletRepository.save(partnerWallet);
                
                // Cộng tiền Student
                studentWallet.setBalance(studentWallet.getBalance().add(rewardAmount));
                walletRepository.save(studentWallet);

                // Ghi giao dịch cho Student (nhận tiền)
                WalletTransaction studentTx = new WalletTransaction();
                studentTx.setWallet(studentWallet);
                studentTx.setCounterparty(partnerWallet);
                studentTx.setTxnType("CHECKIN_REWARD");
                studentTx.setAmount(rewardAmount);
                studentTx.setReferenceType("CHECKIN");
                studentTx.setReferenceId(checkin.getId()); // Sẽ set sau khi checkin được save
                
                // Ghi giao dịch cho Partner (trừ tiền)
                WalletTransaction partnerTx = new WalletTransaction();
                partnerTx.setWallet(partnerWallet);
                partnerTx.setCounterparty(studentWallet);
                partnerTx.setTxnType("EVENT_PAYOUT");
                partnerTx.setAmount(rewardAmount.negate()); // Ghi âm
                partnerTx.setReferenceType("CHECKIN");
                
                // Lưu checkin trước để lấy ID
                checkin.setVerified(true);
                Checkin savedCheckin = checkinRepository.save(checkin);

                studentTx.setReferenceId(savedCheckin.getId());
                partnerTx.setReferenceId(savedCheckin.getId());
                transactionRepository.save(studentTx);
                transactionRepository.save(partnerTx);
                
                rewardGranted = true;
            } else {
                 // Hết ngân sách, vẫn check-in nhưng không thưởng
                 checkin.setVerified(false);
                 checkinRepository.save(checkin);
            }
        } else {
            // Sự kiện không có thưởng
            checkin.setVerified(true);
            checkinRepository.save(checkin);
        }

        return new CheckinResponseDTO(checkin.getId(), student.getFullName(), event.getTitle(), rewardGranted, rewardAmount);
    }
}
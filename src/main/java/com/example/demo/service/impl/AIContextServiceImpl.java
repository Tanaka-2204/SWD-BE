package com.example.demo.service.impl;

import com.example.demo.dto.request.AIHelpRequestDTO;
import com.example.demo.entity.ProductInvoice;
import com.example.demo.entity.Student;
import com.example.demo.entity.Wallet;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.ProductInvoiceRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.service.AIContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AIContextServiceImpl implements AIContextService {

    private final StudentRepository studentRepository;
    private final WalletRepository walletRepository;
    private final ProductInvoiceRepository productInvoiceRepository;
    private final CheckinRepository checkinRepository;

    public AIContextServiceImpl(StudentRepository studentRepository,
                                WalletRepository walletRepository,
                                ProductInvoiceRepository productInvoiceRepository,
                                CheckinRepository checkinRepository) {
        this.studentRepository = studentRepository;
        this.walletRepository = walletRepository;
        this.productInvoiceRepository = productInvoiceRepository;
        this.checkinRepository = checkinRepository;
    }

    @Override
    public Map<String, Object> getStaticContext() {
        Map<String, Object> m = new HashMap<>();
        m.put("features", java.util.List.of(
                "Tham gia sự kiện để nhận COIN",
                "Dùng COIN đổi quà trong cửa hàng",
                "Hủy hóa đơn sẽ hoàn lại COIN và trả lại hàng",
                "Liên hệ đối tác để nhận phần thưởng thực tế"
        ));
        return m;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getHelpAnswer(AIHelpRequestDTO request) {
        UUID userId = request.getUserId();
        Student student = studentRepository.findById(userId).orElse(null);
        Wallet wallet = null;
        if (student != null) {
            wallet = walletRepository.findByOwnerTypeAndOwnerId("STUDENT", student.getId()).orElse(null);
        }

        int recentEvents = 0;
        if (student != null) {
            recentEvents = checkinRepository.findByStudentId(student.getId(), org.springframework.data.domain.PageRequest.of(0, 3)).getNumberOfElements();
        }

        long pendingVouchers = 0;
        if (student != null) {
            pendingVouchers = productInvoiceRepository.countByStatus("PENDING");
        }

        BigDecimal balance = wallet != null ? wallet.getBalance() : BigDecimal.ZERO;

        String answer = "Bạn có thể vào mục 'Cửa hàng' để chọn sản phẩm và nhấn 'Đổi quà'. Mỗi sản phẩm sẽ trừ số COIN tương ứng trong ví của bạn.";

        Map<String, Object> resp = new HashMap<>();
        resp.put("answer", answer);
        resp.put("context", Map.of(
                "userName", student != null ? student.getFullName() : null,
                "wallet", balance,
                "recentEventsJoined", recentEvents,
                "pendingVouchers", pendingVouchers,
                "question", request.getQuestion()
        ));
        return resp;
    }
}
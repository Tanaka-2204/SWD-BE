package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CheckinResponseDTO {
    private Long checkinId;
    private Long eventId;
    private String eventTitle;
    private Long studentId;
    private String studentName;
    private OffsetDateTime registrationTime; // Thời gian đăng ký
    private Boolean verified; // Sẽ luôn là 'false' khi mới đăng ký
    private Integer depositPaid; // Số điểm cọc đã trả
}
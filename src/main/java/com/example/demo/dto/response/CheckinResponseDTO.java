package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CheckinResponseDTO {
    private UUID checkinId;
    private UUID eventId;
    private String eventTitle;
    private UUID studentId;
    private String studentName;
    private OffsetDateTime registrationTime; // Thời gian đăng ký
    private Boolean verified; // Sẽ luôn là 'false' khi mới đăng ký
    private Integer depositPaid; // Số điểm cọc đã trả
}
package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO trả về thông tin sau khi một sinh viên check-in thành công,
 * bao gồm cả trạng thái nhận thưởng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor // Giúp tạo constructor cho new CheckinResponseDTO(...)
public class CheckinResponseDTO {

    /**
     * ID của bản ghi check-in
     */
    private Long checkinId;

    /**
     * Tên sinh viên đã check-in
     */
    private String studentName;

    /**
     * Tên sự kiện đã check-in
     */
    private String eventTitle;

    /**
     * Trạng thái nhận thưởng (true nếu nhận coin thành công)
     */
    private boolean rewardGranted;

    /**
     * Số lượng coin đã nhận (nếu có)
     */
    private BigDecimal rewardAmount;
}
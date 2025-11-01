package com.example.demo.dto.response;

import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Một DTO tùy chỉnh để trả về dữ liệu phân trang cho FE một cách đơn giản.
 * Thay thế hoàn toàn đối tượng Page<T> phức tạp của Spring.
 */
@Data
public class PageResponseDTO<T> {

    // 1. Dữ liệu (ví dụ: danh sách Events)
    private List<T> data;
    
    // 2. Thông tin phân trang (Meta)
    private Meta meta;

    /**
     * Constructor để chuyển đổi từ Page<T> của Spring sang DTO đơn giản này.
     */
    public PageResponseDTO(Page<T> page) {
        this.data = page.getContent();
        this.meta = new Meta(page);
    }

    /**
     * Lớp con chứa thông tin meta (đã được tối giản)
     */
    @Data
    public static class Meta {
        private int currentPage; // <<< Trang hiện tại (bắt đầu từ 1)
        private int pageSize;
        private int totalPages;
        private long totalItems;

        public Meta(Page<?> page) {
            this.currentPage = page.getNumber() + 1; // <<< Chuyển 0-based index thành 1-based
            this.pageSize = page.getSize();
            this.totalPages = page.getTotalPages();
            this.totalItems = page.getTotalElements();
        }
    }
}
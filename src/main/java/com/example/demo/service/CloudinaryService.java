// TẠO TỆP MỚI: service/CloudinaryService.java

package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface CloudinaryService {
    /**
     * Upload file lên Cloudinary và trả về URL an toàn.
     * @param file File được upload từ client
     * @return URL (https) của file đã upload
     * @throws IOException Nếu có lỗi trong quá trình upload
     */
    String uploadFile(MultipartFile file) throws IOException;

    /**
     * Xóa file khỏi Cloudinary (nếu cần)
     * @param publicId ID của file trên Cloudinary
     */
    void deleteFile(String publicId) throws IOException;
}
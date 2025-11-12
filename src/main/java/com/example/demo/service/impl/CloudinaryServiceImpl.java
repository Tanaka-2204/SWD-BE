// TẠO TỆP MỚI: service/impl/CloudinaryServiceImpl.java

package com.example.demo.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.service.CloudinaryService;
import com.example.demo.exception.BadRequestException; // (Hoặc một exception của bạn)
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File to upload is empty");
        }

        File uploadedFile = convertMultiPartToFile(file);
        
        try {
            // Upload lên Cloudinary
            // "folder" là tùy chọn, bạn có thể bỏ qua nếu muốn
            Map uploadResult = cloudinary.uploader().upload(uploadedFile, ObjectUtils.asMap(
                "folder", "student-loyalty-app" 
            ));
            
            // Lấy URL an toàn (https)
            String secureUrl = (String) uploadResult.get("secure_url");
            
            return secureUrl;
        } catch (IOException e) {
            throw new IOException("Could not upload file to Cloudinary", e);
        } finally {
            // Xóa file tạm sau khi upload
            uploadedFile.delete();
        }
    }

    @Override
    public void deleteFile(String publicId) throws IOException {
        // Logic để xóa file (bạn cần trích xuất publicId từ URL)
        // cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    // Helper để chuyển MultipartFile thành File
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
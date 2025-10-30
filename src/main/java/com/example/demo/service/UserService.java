package com.example.demo.service;

import com.example.demo.config.AuthPrincipal;

/**
 * Service để xử lý logic người dùng chung,
 * ví dụ như lấy hồ sơ cá nhân (profile) dựa trên vai trò.
 */
public interface UserService {

    /**
     * Lấy hồ sơ (profile) của người dùng đã xác thực.
     * Tự động kiểm tra vai trò (Admin, Partner, Student) và trả về
     * DTO tương ứng (AdminResponseDTO, PartnerResponseDTO, hoặc StudentResponseDTO).
     *
     * @param principal Đối tượng AuthPrincipal chứa thông tin xác thực và vai trò.
     * @return Một Object là DTO của profile, hoặc ném exception nếu không tìm thấy.
     */
    Object getMyProfile(AuthPrincipal principal);
}
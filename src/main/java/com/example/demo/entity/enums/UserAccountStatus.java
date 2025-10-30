package com.example.demo.entity.enums;

public enum UserAccountStatus {
    /**
     * Tài khoản đang hoạt động bình thường.
     */
    ACTIVE,

    /**
     * Tài khoản đã bị quản trị viên đình chỉ,
     * có thể đăng nhập nhưng không thể thực hiện các hành động chính.
     */
    SUSPENDED
}
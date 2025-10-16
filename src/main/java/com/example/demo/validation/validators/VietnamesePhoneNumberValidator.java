package com.example.demo.validation.validators;

import com.example.demo.validation.annotations.VietnamesePhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class VietnamesePhoneNumberValidator implements ConstraintValidator<VietnamesePhoneNumber, String> {

    // Regex kiểm tra số điện thoại 10 số, bắt đầu bằng 0
    // Ví dụ: 0901234567, 0332221111
    private static final Pattern PHONE_NUMBER_PATTERN = 
        Pattern.compile("^(0?)(3|5|7|8|9)\\d{8}$"); 
    

    @Override
    public void initialize(VietnamesePhoneNumber constraintAnnotation) {
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null) {
            return false; // Hoặc true, tùy thuộc vào việc bạn có @NotNull hay không
        }
        // Loại bỏ khoảng trắng hoặc dấu gạch ngang (nếu có) trước khi kiểm tra
        String cleanedNumber = phoneNumber.replaceAll("[\\s-]+", "");
        
        return PHONE_NUMBER_PATTERN.matcher(cleanedNumber).matches();
    }
}
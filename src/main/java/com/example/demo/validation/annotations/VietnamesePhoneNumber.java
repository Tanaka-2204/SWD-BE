package com.example.demo.validation.annotations;

import com.example.demo.validation.validators.VietnamesePhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = VietnamesePhoneNumberValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface VietnamesePhoneNumber {
    String message() default "Invalid Vietnamese mobile phone number format. (10 digits expected, starting with 03, 05, 07, 08, or 09).";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
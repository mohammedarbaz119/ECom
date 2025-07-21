package com.test.commerce.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class OtpRequest {
    @NotBlank(message = "email is required")
    @Email(message = "Invalid email address")
    private String email;
    @NotBlank(message = "Otp is Required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be exactly 6 digits")
    private String otp;
}
